package com.yoghurt.crypto.transactions.client.util.script;

import java.util.Deque;
import java.util.Iterator;

import com.googlecode.gwt.crypto.bouncycastle.util.Arrays;
import com.yoghurt.crypto.transactions.client.util.transaction.ComputeUtil;
import com.yoghurt.crypto.transactions.client.util.transaction.ScriptOperationUtil;
import com.yoghurt.crypto.transactions.shared.domain.StackObject;

public final class ScriptExecutionUtil {
  private static final byte[] FALSE = new byte[] { 0x00 };

  private static final byte[] TRUE = new byte[] { 0x01 };

  private ScriptExecutionUtil() {}

  public static void execute(final ExecutionStep step) {
    final Deque<StackObject> stack = step.getStack();

    if(ScriptOperationUtil.isDataPushOperation(step.getInstruction().getOperation())) {
      addStackObject(stack, step.getInstruction().getBytes());
    }

    switch(step.getInstruction().getOperation()) {
    case OP_DUP:
      addStackObject(stack, stack.peek());
      break;
    case OP_2DUP:
      final Iterator<StackObject> dup2Iterator = stack.iterator();
      addStackObject(stack, dup2Iterator.next());
      addStackObject(stack, dup2Iterator.next());
      break;
    case OP_3DUP:
      final Iterator<StackObject> dup3Iterator = stack.iterator();
      addStackObject(stack, dup3Iterator.next());
      addStackObject(stack, dup3Iterator.next());
      addStackObject(stack, dup3Iterator.next());
      break;
    case OP_DROP:
      stack.remove();
      break;
    case OP_2DROP:
      stack.remove();
      stack.remove();
      break;
    case OP_CHECKSIG:
      stack.remove();
      stack.remove();

      // TODO Do actual checksig
      addTrue(stack);
      break;
    case OP_EQUALVERIFY:
      final StackObject objA = stack.poll();
      final StackObject objB = stack.poll();

      if(!Arrays.areEqual(objA.getBytes(), objB.getBytes())) {
        addException(step);
      }
      break;
    case OP_HASH160:
      final StackObject poll = stack.poll();

      final byte[] hash160 = ComputeUtil.computeHash160(poll.getBytes());

      addStackObject(stack, hash160);
      break;
    case OP_TRUE:
      addTrue(stack);
      break;
    case OP_FALSE:
      addFalse(stack);
      break;
    default:

    }
  }

  private static void addException(final ExecutionStep step) {
    step.setExecutionError(true);
  }

  private static void addTrue(final Deque<StackObject> stack) {
    addStackObject(stack, TRUE);
  }

  private static void addFalse(final Deque<StackObject> stack) {
    addStackObject(stack, FALSE);
  }

  private static void addStackObject(final Deque<StackObject> stack, final byte[] bytes) {
    addStackObject(stack, new StackObject(bytes));
  }

  private static void addStackObject(final Deque<StackObject> stack, final StackObject object) {
    stack.addFirst(object);
  }
}