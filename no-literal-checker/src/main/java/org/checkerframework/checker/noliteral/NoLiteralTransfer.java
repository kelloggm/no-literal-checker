package org.checkerframework.checker.noliteral;

import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.node.AssignmentNode;
import org.checkerframework.framework.flow.CFAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;

/**
 * The transfer function for the No Literal Checker. This transfer function only handles updating
 * the types of array components based on assignments to array elements, as part of the No Literal
 * Checker's implementation of local type inference for array component types.
 */
public class NoLiteralTransfer extends CFTransfer {

  /** The type factory. */
  private final NoLiteralAnnotatedTypeFactory factory;

  /**
   * Standard constructor.
   *
   * @param analysis the analysis
   */
  public NoLiteralTransfer(CFAnalysis analysis) {
    super(analysis);
    factory = (NoLiteralAnnotatedTypeFactory) analysis.getTypeFactory();
  }

  @Override
  public TransferResult<CFValue, CFStore> visitAssignment(
      AssignmentNode n, TransferInput<CFValue, CFStore> in) {
    factory.modifyTypeAtArrayAccess(n.getTree());
    return super.visitAssignment(n, in);
  }
}
