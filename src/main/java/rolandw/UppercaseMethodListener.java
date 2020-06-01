package rolandw;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.tree.TerminalNode;
import rolandw.antlr.Java9BaseListener;
import rolandw.antlr.Java9Parser;
import rolandw.antlr.Java9Parser.IdentifierContext;

public class UppercaseMethodListener extends Java9BaseListener {

  private List<String> errors = new ArrayList<>();

  public List<String> getErrors() {
    return errors;
  }

  @Override
  public void enterMethodDeclarator(Java9Parser.MethodDeclaratorContext ctx) {
    IdentifierContext node = ctx.identifier();
    String methodName = node.getText();

    if (Character.isUpperCase(methodName.charAt(0))) {
      String error = String.format("Method %s is uppercased!", methodName);
      errors.add(error);
    }
  }
}
