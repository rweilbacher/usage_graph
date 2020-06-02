package rolandw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import rolandw.antlr.Java9BaseListener;
import rolandw.antlr.Java9Parser.ClassDeclarationContext;
import rolandw.antlr.Java9Parser.ClassOrInterfaceTypeContext;
import rolandw.antlr.Java9Parser.ClassTypeContext;
import rolandw.antlr.Java9Parser.CompilationUnitContext;
import rolandw.antlr.Java9Parser.MethodDeclarationContext;
import rolandw.antlr.Java9Parser.MethodInvocationContext;
import rolandw.antlr.Java9Parser.PackageDeclarationContext;
import rolandw.antlr.Java9Parser.ReferenceTypeContext;
import rolandw.antlr.Java9Parser.SimpleTypeNameContext;
import rolandw.antlr.Java9Parser.SingleTypeImportDeclarationContext;
import rolandw.antlr.Java9Parser.TypeNameContext;
import rolandw.antlr.Java9Parser.UnannClassOrInterfaceTypeContext;

public class ImportListener extends Java9BaseListener {
  private List<String> ignoredTypes = Arrays.asList("String", "Integer", "Double", "Long", "Boolean", "Thread", "System");
  private Map<String, TypeDef> importStatementMap = new HashMap<>();
  private List<TypeDef> typeDefs = new ArrayList<>();
  private Set<String> usedTypes = new HashSet<>();
  private Set<String> uncertainTypes = new HashSet<>();
  private String packageName;
  private boolean classStarted;

  @Override
  public void enterSingleTypeImportDeclaration(SingleTypeImportDeclarationContext ctx) {
    String path = ctx.typeName().packageOrTypeName().getText();
    String typeName = ctx.typeName().identifier().getText();
    TypeDef typeDef = TypeDef.of(path + "." + typeName);
    typeDefs.add(typeDef);
    importStatementMap.put(typeName, typeDef);
  }

  @Override
  public void enterPackageDeclaration(PackageDeclarationContext ctx) {
    packageName = ctx.packageName().getText();
  }

  @Override
  public void enterClassOrInterfaceType(ClassOrInterfaceTypeContext ctx) {
//    System.out.println("\ttext=" + ctx.getText());
  }

  @Override
  public void enterClassType(ClassTypeContext ctx) {
//    System.out.println("\ttext=" + ctx.identifier().getText());
  }

  @Override
  public void enterUnannClassOrInterfaceType(UnannClassOrInterfaceTypeContext ctx) {
    String typeName = ctx.getText();
    addTypeName(typeName);
  }

  private void addTypeName(String typeName) {
    TypeDef typeDef = importStatementMap.getOrDefault(typeName, null);
    if (isIgnoredType(typeName, typeDef)) {
      return;
    }
    if (typeDef == null) {
      if (typeName.contains(".")) {
        if (!isIgnoredType(typeName.split("\\.")[0], null)) {
          // Type is fully qualified and can be added to used types
          usedTypes.add(typeName);
        }
      }
      else {
        uncertainTypes.add(typeName);
      }
    }
    else {
      String fullyQualifiedType = typeDef.fullyQualifiedType();
      usedTypes.add(fullyQualifiedType);
    }
  }

  @Override
  public void enterMethodInvocation(MethodInvocationContext ctx) {
    if (ctx.primary() != null) {
      addTypeName(ctx.primary().getText().split("\\.")[0]);
    }
    else if (ctx.typeName() != null) {
      addTypeName(ctx.typeName().getText());
    }

//    System.out.println("----Start----");
//    System.out.println("\tmethodName=" + (ctx.methodName() == null ? "null" : ctx.methodName().getText()));
//    System.out.println("\tprimary=" + (ctx.primary() == null ? "null" : ctx.primary().getText()));
//    System.out.println("\tidentifier=" + (ctx.identifier() == null ? "null" : ctx.identifier().getText()));
//    System.out.println("\texpressionname=" + (ctx.expressionName() == null ? "null" : ctx.expressionName().getText()));
//    System.out.println("\ttypeName=" + (ctx.typeName() == null ? "null" : ctx.typeName().getText()));
//    System.out.println("----Stop----");
  }

  @Override
  public void exitCompilationUnit(CompilationUnitContext ctx) {
    super.exitCompilationUnit(ctx);
  }

  @Override
  public void enterClassDeclaration(ClassDeclarationContext ctx) {
    classStarted = true;
  }

  @Override
  public void enterSimpleTypeName(SimpleTypeNameContext ctx) {
//    usedTypes.add(ctx.identifier().getText());
  }

  @Override
  public void enterReferenceType(ReferenceTypeContext ctx) {
//    System.out.println("\ttext=" + ctx.classOrInterfaceType().getText());
  }

  @Override
  public void enterTypeName(TypeNameContext ctx) {
//    if (!classStarted || ctx.packageOrTypeName() == null) {
//      return;
//    }
//    String packageName = ctx.packageOrTypeName().getText();
//    String typeName = ctx.identifier().getText();
//    usedTypes.add(packageName + "." + typeName);
  }

  private boolean isIgnoredType(String typeName, @Nullable TypeDef typeDef) {
    if (typeDef != null) {
      if (typeDef.path().size() > 0 && ("java".equals(typeDef.path().get(0)) || "javax".equals(typeDef.path().get(0)))) {
        return true;
      }
    }
    if (ignoredTypes.contains(typeName)) {
      return true;
    }
    if (typeName.contains("java.")) {
      return true;
    }
    return false;
  }

  @Override
  public void enterMethodDeclaration(MethodDeclarationContext ctx) {
  }

  public List<TypeDef> importStatements() {
    return typeDefs;
  }

  public Set<String> usedTypes() {
    return usedTypes;
  }

  public String packageName() {
    return packageName;
  }

  public Set<String> uncertainTypes() {
    return uncertainTypes;
  }
}

