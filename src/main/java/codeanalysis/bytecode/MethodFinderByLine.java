package codeanalysis.bytecode;

import model.ClassLocation;
import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.Method;

public class MethodFinderByLine extends ByteCodeMethodFinder {

    private ClassLocation point;

    public MethodFinderByLine(ClassLocation point) {
        this.setPoint(point);
    }

    public ClassLocation getPoint() {
        return point;
    }

    public void setPoint(ClassLocation point) {
        this.point = point;
    }

    public void visitMethod(Method method) {
        if (method.getLineNumberTable() != null) {
            for (LineNumber lineNumber : method.getLineNumberTable().getLineNumberTable()) {
                if (lineNumber.getLineNumber() == point.getLineNumber()) {
                    this.setMethod(method);
                }
            }
        }
    }
}
