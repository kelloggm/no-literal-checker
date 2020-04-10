package org.checkerframework.checker.noliteral;

import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.node.AssignmentNode;
import org.checkerframework.framework.flow.CFAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;

public class NoLiteralTransfer extends CFTransfer {

    private final NoLiteralAnnotatedTypeFactory factory;

    public NoLiteralTransfer(CFAnalysis analysis) {
        super(analysis);
        factory = (NoLiteralAnnotatedTypeFactory) analysis.getTypeFactory();
    }

    @Override
    public TransferResult<CFValue, CFStore> visitAssignment(AssignmentNode n, TransferInput<CFValue, CFStore> in) {
        factory.modifyTypeAtArrayAccess(n.getTree());
        return super.visitAssignment(n, in);
    }
}
