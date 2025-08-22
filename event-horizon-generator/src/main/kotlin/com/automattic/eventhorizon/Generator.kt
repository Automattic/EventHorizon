package com.automattic.eventhorizon

import java.nio.file.Path

public interface Generator {
  public fun generate(schema: Schema, outputPath: Path): Path
}
