package rolandw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TypeDef {
  private String typeName;
  private List<String> path;

  private TypeDef(String fullyQualifiedTypeName) {
    path = new ArrayList<>();
    String[] parts = fullyQualifiedTypeName.split("\\.");
    path.addAll(Arrays.asList(parts).subList(0, parts.length - 1));
    typeName = parts[parts.length - 1];
  }

  public static TypeDef of(String fullyQualifiedTypeName) {
    return new TypeDef(fullyQualifiedTypeName);
  }

  public String className() {
    return typeName;
  }

  public List<String> path() {
    return path;
  }

  public String fullyQualifiedType() {
    String pathString = pathString();
    return pathString + typeName;
  }

  public String pathString() {
    StringBuilder pathString = new StringBuilder();
    path.forEach(packageName -> {
      pathString.append(packageName);
      pathString.append('.');
    });
    return pathString.toString();
  }

  @Override
  public String toString() {
    return fullyQualifiedType();
  }
}
