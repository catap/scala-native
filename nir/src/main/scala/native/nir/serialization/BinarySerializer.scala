package native
package nir
package serialization

import java.nio.ByteBuffer
import native.nir.{Tags => T}

final class BinarySerializer(buffer: ByteBuffer) {
  import buffer._

  final def serialize(defns: Seq[Defn]) = putDefns(defns)

  private def putSeq[T](putT: T => Unit)(seq: Seq[T]) = {
    putInt(seq.length)
    seq.foreach(putT)
  }

  private def putString(v: String) = {
    val bytes = v.getBytes
    putInt(bytes.length); put(bytes)
  }

  private def putAttrs(attrs: Seq[Attr]) = putSeq(putAttr)(attrs)
  private def putAttr(attr: Attr) = attr match {
    case Attr.Usgn => putInt(T.UsgnAttr)
  }

  private def putBin(bin: Bin) = bin match {
    case Bin.Add  => putInt(T.AddBin)
    case Bin.Sub  => putInt(T.SubBin)
    case Bin.Mul  => putInt(T.MulBin)
    case Bin.Div  => putInt(T.DivBin)
    case Bin.Mod  => putInt(T.ModBin)
    case Bin.Shl  => putInt(T.ShlBin)
    case Bin.Lshr => putInt(T.LshrBin)
    case Bin.Ashr => putInt(T.AshrBin)
    case Bin.And  => putInt(T.AndBin)
    case Bin.Or   => putInt(T.OrBin)
    case Bin.Xor  => putInt(T.XorBin)
  }

  private def putComp(comp: Comp) = comp match {
    case Comp.Eq  => putInt(T.EqComp)
    case Comp.Neq => putInt(T.NeqComp)
    case Comp.Lt  => putInt(T.LtComp)
    case Comp.Lte => putInt(T.LteComp)
    case Comp.Gt  => putInt(T.GtComp)
    case Comp.Gte => putInt(T.GteComp)
  }

  private def putConv(conv: Conv) = conv match {
    case Conv.Trunc    => putInt(T.TruncConv)
    case Conv.Zext     => putInt(T.ZextConv)
    case Conv.Sext     => putInt(T.SextConv)
    case Conv.Fptrunc  => putInt(T.FptruncConv)
    case Conv.Fpext    => putInt(T.FpextConv)
    case Conv.Fptoui   => putInt(T.FptouiConv)
    case Conv.Fptosi   => putInt(T.FptosiConv)
    case Conv.Uitofp   => putInt(T.UitofpConv)
    case Conv.Sitofp   => putInt(T.SitofpConv)
    case Conv.Ptrtoint => putInt(T.PtrtointConv)
    case Conv.Inttoptr => putInt(T.InttoptrConv)
    case Conv.Bitcast  => putInt(T.BitcastConv)
  }

  private def putDefns(defns: Seq[Defn]): Unit = putSeq(putDefn)(defns)
  private def putDefn(value: Defn): Unit = value match {
    case Defn.Var(name, ty, value) =>
      putInt(T.VarDefn); putName(name); putType(ty); putVal(value)
    case Defn.Declare(name, ty) =>
      putInt(T.DeclareDefn); putName(name); putType(ty)
    case Defn.Define(name, ty, blocks) =>
      putInt(T.DefineDefn); putName(name); putType(ty); putBlocks(blocks)
    case Defn.Struct(name, members) =>
      putInt(T.StructDefn); putName(name); putDefns(members)
    case Defn.Interface(name, ifaces, members) =>
      putInt(T.IntefaceDefn); putName(name); putTypes(ifaces); putDefns(members)
    case Defn.Class(name, parent, ifaces, members) =>
      putInt(T.ClassDefn); putName(name); putType(parent); putTypes(ifaces); putDefns(members)
    case Defn.Module(name, parent, ifaces, members) =>
      putInt(T.ModuleDefn); putName(name); putType(parent); putTypes(ifaces); putDefns(members)
  }

  private def putBlocks(blocks: Seq[Block]) = putSeq(putBlock)(blocks)
  private def putBlock(block: Block) = {
    putName(block.name)
    putParams(block.params)
    putInstrs(block.instrs)
  }

  private def putInstrs(instrs: Seq[Instr]) = putSeq(putInstr)(instrs)
  private def putInstr(instr: Instr) = {
    putName(instr.name)
    putAttrs(instr.attrs)
    putOp(instr.op)
  }

  private def putParams(params: Seq[Param]) = putSeq(putParam)(params)
  private def putParam(param: Param) = {
    putName(param.name)
    putType(param.ty)
  }

  private def putNexts(nexts: Seq[Next]) = putSeq(putNext)(nexts)
  private def putNext(next: Next) = {
    putName(next.name)
    putVals(next.args)
  }

  private def putCases(kases: Seq[Case]) = putSeq(putCase)(kases)
  private def putCase(kase: Case) = {
    putVal(kase.value)
    putNext(kase.next)
  }

  private def putNames(names: Seq[Name]): Unit = putSeq(putName)(names)
  private def putName(name: Name): Unit = name match {
    case Name.None                  => putInt(T.NoneName)
    case Name.Fresh(id)             => putInt(T.FreshName); putInt(id)
    case Name.Local(id)             => putInt(T.LocalName); putString(id)
    case Name.Prim(id)              => putInt(T.PrimName); putString(id)
    case Name.Foreign(id)           => putInt(T.ForeignName); putString(id)
    case Name.Nested(owner, member) => putInt(T.NestedName); putName(owner); putName(member)
    case Name.Class(id)             => putInt(T.ClassName); putString(id)
    case Name.Module(id)            => putInt(T.ModuleName); putString(id)
    case Name.Interface(id)         => putInt(T.InterfaceName); putString(id)
    case Name.Field(id)             => putInt(T.FieldName); putString(id)
    case Name.Constructor(args)     => putInt(T.ConstructorName); putNames(args)
    case Name.Method(id, args, ret) => putInt(T.MethodName); putString(id); putNames(args); putName(ret)
    case Name.Array(of)             => putInt(T.ArrayName); putName(of)
    case Name.Tagged(n, tag)        => putInt(T.TaggedName); putName(n); putString(tag)
  }

  private def putOp(op: Op) = op match {
    case Op.Undefined =>
      putInt(T.UndefinedOp)
    case Op.Ret(v) =>
      putInt(T.RetOp); putVal(v)
    case Op.Throw(v) =>
      putInt(T.ThrowOp); putVal(v)
    case Op.Jump(next) =>
      putInt(T.JumpOp); putNext(next)
    case Op.If(v, thenp, elsep) =>
      putInt(T.IfOp); putVal(v); putNext(thenp); putNext(elsep)
    case Op.Switch(v, default, cases) =>
      putInt(T.SwitchOp); putVal(v); putNext(default); putCases(cases)
    case Op.Invoke(f, args, succ, fail) =>
      putInt(T.InvokeOp); putVal(f); putVals(args); putNext(succ); putNext(fail)

    case Op.Call(ty, v, args) =>
      putInt(T.CallOp); putType(ty); putVal(v); putVals(args)
    case Op.Load(ty, ptr) =>
      putInt(T.LoadOp); putType(ty); putVal(ptr)
    case Op.Store(ty, value, ptr) =>
      putInt(T.StoreOp); putType(ty); putVal(value); putVal(ptr)
    case Op.Elem(ty, v, indexes) =>
      putInt(T.ElemOp); putType(ty); putVal(v); putVals(indexes)
    case Op.Extract(ty, v, index) =>
      putInt(T.ExtractOp); putType(ty); putVal(v); putVal(index)
    case Op.Insert(ty, v, value, index) =>
      putInt(T.InsertOp); putType(ty); putVal(v); putVal(value); putVal(index)
    case Op.Alloc(ty) =>
      putInt(T.AllocOp); putType(ty)
    case Op.Alloca(ty) =>
      putInt(T.AllocaOp); putType(ty)
    case Op.Size(ty) =>
      putInt(T.SizeOp); putType(ty)
    case Op.Bin(bin, ty, l, r) =>
      putInt(T.BinOp); putBin(bin); putType(ty); putVal(l); putVal(r)
    case Op.Comp(comp, ty, l, r) =>
      putInt(T.CompOp); putComp(comp); putType(ty); putVal(l); putVal(r)
    case Op.Conv(conv, ty, v) =>
      putInt(T.ConvOp); putConv(conv); putType(ty); putVal(v)

    case Op.FieldElem(ty, name, v) =>
      putInt(T.FieldElemOp); putName(name); putVal(v)
    case Op.MethodElem(ty, name, v) =>
      putInt(T.MethodElemOp); putType(ty); putName(name); putVal(v)
    case Op.AllocClass(ty) =>
      putInt(T.AllocClassOp); putType(ty)
    case Op.AllocArray(ty, v) =>
      putInt(T.AllocArrayOp); putType(ty); putVal(v)
    case Op.Equals(l, r) =>
      putInt(T.EqualsOp); putVal(l); putVal(r)
    case Op.HashCode(v) =>
      putInt(T.HashCodeOp); putVal(v)
    case Op.GetClass(v) =>
      putInt(T.GetClassOp); putVal(v)
    case Op.AsInstanceOf(ty, v) =>
      putInt(T.AsInstanceOfOp); putType(ty); putVal(v)
    case Op.IsInstanceOf(ty, v) =>
      putInt(T.IsInstanceOfOp); putType(ty); putVal(v)
    case Op.ArrayLength(v) =>
      putInt(T.ArrayLengthOp); putVal(v)
    case Op.ArrayElem(ty, v, index) =>
      putInt(T.ArrayElemOp); putType(ty); putVal(v); putVal(index)
    case Op.Box(ty, v) =>
      putInt(T.BoxOp); putType(ty); putVal(v)
    case Op.Unbox(ty, v) =>
      putInt(T.UnboxOp); putType(ty); putVal(v)
    case Op.MonitorEnter(v) =>
      putInt(T.MonitorEnterOp); putVal(v)
    case Op.MonitorExit(v) =>
      putInt(T.MonitorExitOp); putVal(v)
    case Op.StringConcat(l, r) =>
      putInt(T.StringConcatOp); putVal(l); putVal(r)
    case Op.ToString(v, radix) =>
      putInt(T.ToStringOp); putVal(v); putVal(radix)
    case Op.FromString(ty, v, radix) =>
      putInt(T.FromStringOp); putType(ty); putVal(v); putVal(radix)
  }

  private def putTypes(tys: Seq[Type]): Unit = putSeq(putType)(tys)
  private def putType(ty: Type): Unit = ty match {
    case Type.None                => putInt(T.NoneType)
    case Type.Void                => putInt(T.VoidType)
    case Type.Size                => putInt(T.SizeType)
    case Type.Bool                => putInt(T.BoolType)
    case Type.I8                  => putInt(T.I8Type)
    case Type.I16                 => putInt(T.I16Type)
    case Type.I32                 => putInt(T.I32Type)
    case Type.I64                 => putInt(T.I64Type)
    case Type.F32                 => putInt(T.F32Type)
    case Type.F64                 => putInt(T.F64Type)
    case Type.Array(ty, n)        => putInt(T.ArrayType); putType(ty); putInt(n)
    case Type.Ptr(ty)             => putInt(T.PtrType); putType(ty)
    case Type.Function(args, ret) => putInt(T.FunctionType); putTypes(args); putType(ret)
    case Type.Struct(n)           => putInt(T.StructType); putName(n)

    case Type.Unit                => putInt(T.UnitType)
    case Type.Nothing             => putInt(T.NothingType)
    case Type.NullClass           => putInt(T.NullClassType)
    case Type.ObjectClass         => putInt(T.ObjectClassType)
    case Type.ClassClass          => putInt(T.ClassClassType)
    case Type.StringClass         => putInt(T.StringClassType)
    case Type.CharacterClass      => putInt(T.CharacterClassType)
    case Type.BooleanClass        => putInt(T.BooleanClassType)
    case Type.ByteClass           => putInt(T.ByteClassType)
    case Type.ShortClass          => putInt(T.ShortClassType)
    case Type.IntegerClass        => putInt(T.IntegerClassType)
    case Type.LongClass           => putInt(T.LongClassType)
    case Type.FloatClass          => putInt(T.FloatClassType)
    case Type.DoubleClass         => putInt(T.DoubleClassType)
    case Type.Class(n)            => putInt(T.ClassType); putName(n)
    case Type.ArrayClass(ty)      => putInt(T.ArrayClassType); putType(ty)
  }

  private def putVals(values: Seq[Val]): Unit = putSeq(putVal)(values)
  private def putVal(value: Val): Unit = value match {
    case Val.None           => putInt(T.NoneVal)
    case Val.True           => putInt(T.TrueVal)
    case Val.False          => putInt(T.FalseVal)
    case Val.Zero(ty)       => putInt(T.ZeroVal); putType(ty)
    case Val.I8(v)          => putInt(T.I8Val); put(v)
    case Val.I16(v)         => putInt(T.I16Val); putShort(v)
    case Val.I32(v)         => putInt(T.I32Val); putInt(v)
    case Val.I64(v)         => putInt(T.I64Val); putLong(v)
    case Val.F32(v)         => putInt(T.F32Val); putFloat(v)
    case Val.F64(v)         => putInt(T.F64Val); putDouble(v)
    case Val.Struct(ty, vs) => putInt(T.StructVal); putType(ty); putVals(vs)
    case Val.Array(ty, vs)  => putInt(T.ArrayVal); putType(ty); putVals(vs)
    case Val.Name(n, ty)    => putInt(T.NameVal); putName(n); putType(ty)

    case Val.Unit           => putInt(T.UnitVal)
    case Val.Null           => putInt(T.NullVal)
    case Val.String(v)      => putInt(T.StringVal); putString(v)
    case Val.Class(ty)      => putInt(T.ClassVal); putType(ty)
  }
}