package rolandw;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.Rasterizer;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import rolandw.antlr.Java9Lexer;
import rolandw.antlr.Java9Parser;
import rolandw.antlr.Java9Parser.CompilationUnitContext;


/**
 * Hello world!
 */
public class App {

  // Fully classified class name -> JavaFile
  Map<String, JavaFile> javaFileMap = new HashMap<>();


  public static void main(String[] args) throws IOException {
    App app = new App();
    app.start();
  }

  private void start() throws IOException {
    Path currentRelativePath = Paths.get("");
    traverseFiles(currentRelativePath.resolve("xdman"));

    System.out.println("--------------------------------------------");
    Map<String, MutableNode> nodeMap = new HashMap<>();
    MutableGraph graph = Factory.mutGraph().setDirected(true);
    for (Map.Entry<String, JavaFile> entry : javaFileMap.entrySet()) {
      String fullyQualifiedTypeName = entry.getKey();
      JavaFile javaFile = entry.getValue();
      System.out.println(javaFile.fullyQualifiedType() + "=" + fullyQualifiedTypeName);
      printIterable(javaFile.usedTypes(), "usedTypes");

      MutableNode currNode = addIfMissing(nodeMap, graph, fullyQualifiedTypeName);
      for (String usedType : javaFile.usedTypes()) {
        MutableNode linkedNode = addIfMissing(nodeMap, graph, usedType);
        currNode.addLink(linkedNode);
      }
    }
    Graphviz.fromGraph(graph).scale(2.0).render(Format.PNG).toFile(new File("out.png"));
    Graphviz.fromGraph(graph).rasterize(Rasterizer.BATIK).toFile(new File("out_rasterized.png"));
    Graphviz.fromGraph(graph).render(Format.DOT).toFile(new File("out.dot"));
  }

  private MutableNode addIfMissing(Map<String, MutableNode> map, MutableGraph graph,
      String nodeContent) {
    if (map.containsKey(nodeContent)) {
      return map.get(nodeContent);
    } else {
      MutableNode node = Factory.mutNode(nodeContent);
      map.put(nodeContent, node);
      graph.add(node);
      return node;
    }
  }

  private void traverseFiles(Path startPath) throws IOException {
    // Class Name -> JavaFile
    Map<String, JavaFile> packageFiles = new HashMap<>();

    Iterator<File> fileIterator = FileUtils
        .iterateFiles(startPath.toFile(), new String[]{"java"}, false);

    while (fileIterator.hasNext()) {
      File file = fileIterator.next();
      Java9Lexer lexer = null;
      try {
        lexer = new Java9Lexer(CharStreams.fromStream(new FileInputStream(file)));
      } catch (IOException e) {
        e.printStackTrace();
      }
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      Java9Parser parser = new Java9Parser(tokens);
      CompilationUnitContext compilationUnit = parser.compilationUnit();
      ParseTreeWalker walker = new ParseTreeWalker();
      ImportListener listener = new ImportListener();
      walker.walk(listener, compilationUnit);

      String typeName = file.toPath().getFileName().toString().split("\\.")[0];
      String fullyQualifiedTypeName = listener.packageName() + "." + typeName;
//      System.out.println(fullyQualifiedTypeName);

      JavaFile javaFile = new JavaFile(
          TypeDef.of(fullyQualifiedTypeName),
          listener.usedTypes(),
          listener.uncertainTypes());

      javaFileMap.put(fullyQualifiedTypeName, javaFile);
      packageFiles.put(typeName, javaFile);
//      printIterable(listener.usedTypes(), "usedTypes");
    }
    // Resolve types in this package
    for (Map.Entry<String, JavaFile> entry : packageFiles.entrySet()) {
      entry.getValue().resolveUncertainTypes(packageFiles.keySet());
    }

    IOFileFilter fileFilter = new IOFileFilter() {
      @Override
      public boolean accept(File file) {
        return false;
      }

      @Override
      public boolean accept(File file, String s) {
        return false;
      }
    };

    IOFileFilter dirFilter = new IOFileFilter() {
      int depth;

      @Override
      public boolean accept(File file) {
        String separator = FileSystems.getDefault().getSeparator();
        separator += separator;
        Path path = file.toPath();
        int pathLength = path.toAbsolutePath().toString()
            .split(separator).length;
        int startPathLength = startPath.toAbsolutePath().toString()
            .split(separator).length;
        return (pathLength - startPathLength) == 1;
      }

      @Override
      public boolean accept(File file, String s) {
        return false;
      }
    };

    Iterator<File> dirIterator = FileUtils
        .iterateFilesAndDirs(startPath.toFile(), fileFilter, dirFilter);

    dirIterator.next();
    while (dirIterator.hasNext()) {
      File dir = dirIterator.next();
      try {
        traverseFiles(dir.toPath());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static <T> void printIterable(Iterable<T> list, String iterableName) {
    System.out.println(iterableName + "{");
    for (T item : list) {
      System.out.println("\t" + item.toString());
    }
    System.out.println("}");
  }
}
