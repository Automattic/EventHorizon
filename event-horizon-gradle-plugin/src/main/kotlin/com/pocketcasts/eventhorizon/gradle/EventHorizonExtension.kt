package com.pocketcasts.eventhorizon.gradle

import javax.inject.Inject
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

public abstract class EventHorizonExtension @Inject constructor(
  objects: ObjectFactory,
) {
  public val packageName: Property<String> = objects.property(String::class.java)

  public val inputFile: RegularFileProperty = objects.fileProperty()
}
