package org.brapi.schematools.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import lombok.AllArgsConstructor;
import org.brapi.schematools.core.model.BrAPISchema;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.nio.file.Files.find;

@AllArgsConstructor
public class BrAPISchemaReader {

  private static final Pattern FILE_NAME_PATTERN = Pattern.compile("^(\\w+)\\.json$");
  private final JsonSchemaFactory factory ;

  public BrAPISchemaReader() {
    factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
  }

  public List<BrAPISchema> read(Path schemaDirectory) throws BrAPISchemaReaderException {
    try {
      return find(schemaDirectory, 2, this::schemaPathMatcher).flatMap(this::createBrAPISchemas).toList() ;
    } catch (IOException e) {
      throw new BrAPISchemaReaderException(e);
    }
  }

  private Stream<BrAPISchema> createBrAPISchemas(Path path) {
    JsonSchema schema = factory.getSchema(path.toUri());

    JsonNode json = schema.getSchemaNode() ;

    JsonNode defs = json.get("$defs");

    String module = path.getParent().getFileName().toString() ;

    if (defs != null) {
      json = defs ;
    }

    Iterator<Map.Entry<String, JsonNode>> iterator = json.fields();
    return Stream.generate(() -> null)
            .takeWhile(x -> iterator.hasNext())
            .map(n -> iterator.next()).map(entry -> BrAPISchema.builder().
                    name(entry.getKey()).
                    module(module).
                    schema(entry.getValue()).
                    build());
  }

  private String findName(Path path) {
    Matcher matcher = FILE_NAME_PATTERN.matcher(path.getFileName().toString());

    if (matcher.matches()) {
      return matcher.group(1) ;
    } else {
      return path.getFileName().toString() ;
    }
  }

  private boolean schemaPathMatcher(Path path, BasicFileAttributes basicFileAttributes) {
    return basicFileAttributes.isRegularFile() ;
  }
}
