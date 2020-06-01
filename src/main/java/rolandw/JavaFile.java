package rolandw;

import java.util.Set;

public class JavaFile {
  private TypeDef type;
  private Set<String> usedTypes;
  private Set<String> uncertainTypes;


  public JavaFile(TypeDef type, Set<String> usedTypes, Set<String> uncertainTypes) {
    this.type = type;
    this.usedTypes = usedTypes;
    this.uncertainTypes = uncertainTypes;
  }

  public String fullyQualifiedType() {
    return type.fullyQualifiedType();
  }

  public TypeDef type() {
    return type;
  }

  public Set<String> usedTypes() {
    return usedTypes;
  }

  public void resolveUncertainTypes(Set<String> packageTypeNames) {
    String pathString = type.pathString();
    for (String typeName : packageTypeNames) {
      if (uncertainTypes.contains(typeName)) {
        uncertainTypes.remove(typeName);
        usedTypes.add(pathString + typeName);
      }
    }
  }
}
