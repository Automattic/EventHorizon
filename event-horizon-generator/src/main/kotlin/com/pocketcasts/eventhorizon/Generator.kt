package com.pocketcasts.eventhorizon

import java.nio.file.Path

public interface Generator {
  public fun generate(events: Events, outputDir: Path): Path
}
