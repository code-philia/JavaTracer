package org.cophi.javatracer.instrumentation.instrumentator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.bcel.Const;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.generic.AASTORE;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.Type;
import org.cophi.javatracer.configs.ProjectConfig;
import org.cophi.javatracer.log.Log;
import org.cophi.javatracer.utils.BasicTypeUtils;

public abstract class AbstractInstrumentator implements JavaTracerInstrumentator {

    protected static final String CLASS_NAME_VAR_NAME = "$className";
    protected static final String METHOD_SIGNATURE_VAR_NAME = "$methodSignature";

    protected ProjectConfig projectConfig;

    public AbstractInstrumentator(final ProjectConfig projectConfig) {
        this.projectConfig = projectConfig;
    }

    public byte[] instrument(final String className, byte[] classfileBuffer) throws IOException {
        ClassParser parser = new ClassParser(new ByteArrayInputStream(classfileBuffer), className);
        JavaClass javaClass = parser.parse();
        // Do not instrument interfaces
        if (!javaClass.isClass()) {
            return classfileBuffer;
        }
        return this.instrument(className, javaClass);
    }

    protected LocalVariableGen createMethodParamTypesObjectArrayVar(MethodGen methodGen,
        ConstantPoolGen constPool,
        InstructionHandle startInsn, InstructionList newInsns, String varName) {
        /* init Object[] */
        LocalVariableGen argObjsVar = methodGen.addLocalVariable(varName,
            new ArrayType(Type.OBJECT, 1), startInsn,
            startInsn.getNext());
        newInsns.append(new PUSH(constPool, methodGen.getArgumentTypes().length));
        newInsns.append(new ANEWARRAY(constPool.addClass(Object.class.getName())));
        argObjsVar.setStart(newInsns.append(new ASTORE(argObjsVar.getIndex())));
        /* assign method argument values to Object[] */
        LocalVariableTable localVariableTable = methodGen.getLocalVariableTable(constPool);
        if (localVariableTable != null) {
            int varIdx = (Const.ACC_STATIC & methodGen.getAccessFlags()) != 0 ? 0 : 1;
            for (int i = 0; i < methodGen.getArgumentTypes().length; i++) {
                LocalVariable localVariable = localVariableTable.getLocalVariable(varIdx, 0);
                if (localVariable == null) {
                    Log.warn("localVariable is empty, varIdx=" + varIdx);
                    break;
                }
                newInsns.append(new ALOAD(argObjsVar.getIndex()));
                newInsns.append(new PUSH(constPool, i));
                Type argType = methodGen.getArgumentType(i);
                newInsns.append(InstructionFactory.createLoad(argType, localVariable.getIndex()));
                if (argType instanceof BasicType) {
                    newInsns.append(
                        new INVOKESTATIC(BasicTypeUtils.getValueOfMethodIdx((BasicType) argType,
                            constPool)));
                }
                newInsns.append(new AASTORE());
                if (Type.DOUBLE.equals(argType) || Type.LONG.equals(argType)) {
                    varIdx += 2;
                } else {
                    varIdx++;
                }
            }
        } else {
            Log.warn("localVariableTable is empty!");
        }
        return argObjsVar;
    }

    protected abstract byte[] instrument(final String className, final JavaClass javaClass);
}
