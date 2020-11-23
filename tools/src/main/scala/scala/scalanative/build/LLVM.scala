package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths, StandardCopyOption}
import java.util.Arrays
import scala.sys.process._
import scalanative.build.IO.RichPath
import scalanative.build.NativeLib._

/** Internal utilities to interact with LLVM command-line tools. */
private[scalanative] object LLVM {

  /**
   * Called to unpack jars and copy native code.
   *
   * @param nativelib the native lib to copy/unpack
   * @return The destination path of the directory
   */
  def unpackNativeCode(nativelib: NativeLib): Path =
    if (NativeLib.isJar(nativelib)) unpackNativeJar(nativelib)
    else copyNativeDir(nativelib)

  /**
   * Unpack the `src` Jar Path to `workdir/dest` where `dest`
   * is the generated directory where the Scala Native lib or
   * a third party library that includes native code is copied.
   *
   * If the same archive has already been unpacked to this location
   * and hasn't changed, this call has no effect.
   *
   * @param nativelib The NativeLib to unpack.
   * @return The Path where the nativelib has been unpacked, `workdir/dest`.
   */
  private def unpackNativeJar(nativelib: NativeLib): Path = {
    val target      = nativelib.dest
    val source      = nativelib.src
    val jarhash     = IO.sha1(source)
    val jarhashPath = target.resolve("jarhash")
    def unpacked =
      Files.exists(target) &&
        Files.exists(jarhashPath) &&
        Arrays.equals(jarhash, Files.readAllBytes(jarhashPath))

    if (!unpacked) {
      IO.deleteRecursive(target)
      IO.unzip(source, target)
      IO.write(jarhashPath, jarhash)
    }
    target
  }

  /**
   * Copy project code from project `src` Path to `workdir/dest`
   * Path where it can be compiled and linked.
   *
   * This does not copy if no native code has changed.
   *
   * @param nativelib The NativeLib to copy.
   * @return The Path where the code was copied, `workdir/dest`.
   */
  private def copyNativeDir(nativelib: NativeLib): Path = {
    val target        = nativelib.dest
    val source        = nativelib.src
    val files         = IO.getAll(source, NativeLib.allFilesPattern(source))
    val fileshash     = IO.sha1files(files)
    val fileshashPath = target.resolve("fileshash")
    def copied =
      Files.exists(target) &&
        Files.exists(fileshashPath) &&
        Arrays.equals(fileshash, Files.readAllBytes(fileshashPath))
    if (!copied) {
      IO.deleteRecursive(target)
      IO.copyDirectory(source, target)
      IO.write(fileshashPath, fileshash)
    }
    target
  }

  /**
   * Compile all the native lib source files to `.o` files
   * with special logic to select GC and optional components
   * for the Scala Native `nativelib`.
   *
   * @param config        The configuration of the toolchain.
   * @param linkerResult  The results from the linker.
   * @param nativelibPath The generated location of the Scala Native nativelib.
   * @param nativelibs    The Paths to the native libs
   * @return              The paths to produced '.o' files
   */
  def compileNativelibs(config: Config,
                        linkerResult: linker.Result,
                        nativelibs: Seq[Path],
                        nativelibPath: Path): Seq[Path] = {
    val workdir = config.workdir
    // search starting at workdir `native` to find
    // code across all native component libraries
    // including the `nativelib`
    val srcPatterns = NativeLib.destSrcPatterns(workdir, nativelibs)
    val paths       = IO.getAll(workdir, srcPatterns).map(_.abs)
    val libPath     = nativelibPath.resolve(NativeLib.codeDir)

    // predicate to check if given file path shall be compiled
    // we only include sources of the current gc and exclude
    // all optional dependencies if they are not necessary
    val optPath = libPath.resolve("optional").abs
    val (gcPath, gcSelPath) = {
      val gcPath    = libPath.resolve("gc")
      val gcSelPath = gcPath.resolve(config.gc.name)
      (gcPath.abs, gcSelPath.abs)
    }

    def include(path: String) = {
      if (path.contains(optPath)) {
        val name = Paths.get(path).toFile.getName.split("\\.").head
        linkerResult.links.map(_.name).contains(name)
      } else if (path.contains(gcPath)) {
        path.contains(gcSelPath)
      } else {
        true
      }
    }

    // delete .o files for all excluded source files
    paths.foreach { path =>
      if (!include(path)) {
        val opath = Paths.get(path + oExt)
        if (Files.exists(opath)) {
          Files.delete(opath)
        }
      }
    }

    // generate .o files for all included source files in parallel
    paths
      .filter(include)
      .par
      .map { path =>
        val opath = path + oExt
        if (!Files.exists(Paths.get(opath))) {
          val isCpp    = path.endsWith(cppExt)
          val compiler = if (isCpp) config.clangPP.abs else config.clang.abs
          val stdflag  = if (isCpp) "-std=c++11" else "-std=gnu11"
          val flags = clangOptions(
            config,
            Seq(stdflag, "-fvisibility=hidden") ++ config.compileOptions)
          val compilec = Seq(compiler) ++ flags ++ Seq("-c", path, "-o", opath)

          config.logger.running(compilec)
          val result = Process(compilec, config.workdir.toFile) ! Logger
            .toProcessLogger(config.logger)
          if (result != 0) {
            sys.error("Failed to compile native library runtime code.")
          }
        }
        Paths.get(opath)
      }
      .seq
  }

  /** Compile the given LL files to object files */
  def compile(config: Config, llPaths: Seq[Path]): Seq[Path] = {
    val optimizationOpt =
      config.mode match {
        case Mode.Debug       => "-O0"
        case Mode.ReleaseFast => "-O2"
        case Mode.ReleaseFull => "-O3"
      }

    val flags =
      clangOptions(config, config.compileOptions ++ Seq(optimizationOpt))

    llPaths.par.map { ll =>
      val apppath = ll.abs
      val outpath = apppath + oExt
      val compile =
        Seq(config.clang.abs) ++ flags ++ Seq("-c", apppath, "-o", outpath)

      config.logger.running(compile)
      Process(compile, config.workdir.toFile) ! Logger.toProcessLogger(
        config.logger)
      Paths.get(outpath)
    }.seq
  }

  /**
   * Links a collection of `.ll` files and the `.o` files
   * from the `nativelib`, other libaries, and the
   * application project into the native binary.
   *
   * @param config       The configuration of the toolchain.
   * @param linkerResult The results from the linker.
   * @param llPaths      The list of `.ll` files to link.
   * @param nativelibs   The Paths to the native libs
   * @param outpath      The path where to write the resulting binary.
   * @return `outpath`
   */
  def link(config: Config,
           linkerResult: linker.Result,
           objectFiles: Seq[Path],
           outpath: Path): Path = {
    val links = {
      val srclinks = linkerResult.links.map(_.name)
      val gclinks  = config.gc.links
      // We need extra linking dependencies for:
      // * libdl for our vendored libunwind implementation.
      // * libpthread for process APIs and parallel garbage collection.
      "pthread" +: "dl" +: srclinks ++: gclinks
    }
    val linkopts = config.linkingOptions ++ links.map("-l" + _)
    val flags    = clangOptions(config, Seq("-rdynamic"))
    val paths    = objectFiles.map(_.abs)
    val compile =
      Seq(config.clangPP.abs) ++ flags ++ Seq("-o", outpath.abs) ++ paths ++ linkopts
    val ltoName = lto(config).getOrElse("none")

    config.logger.time(
      s"Linking native code (${config.gc.name} gc, $ltoName lto)") {
      config.logger.running(compile)
      Process(compile, config.workdir.toFile) ! Logger.toProcessLogger(
        config.logger)
    }
    outpath
  }

  private def lto(config: Config): Option[String] =
    (config.mode, config.LTO) match {
      case (Mode.Debug, _)             => None
      case (_: Mode.Release, LTO.None) => None
      case (_: Mode.Release, lto)      => Some(lto.name)
    }

  private def flto(config: Config): Seq[String] =
    lto(config).fold[Seq[String]] {
      Seq()
    } { name => Seq(s"-flto=$name") }

  private def clangOptions(config: Config, opts: Seq[String]): Seq[String] =
    flto(config) ++ opts ++ Seq("-Wno-override-module")
}
