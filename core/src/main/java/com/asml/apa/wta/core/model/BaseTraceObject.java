package com.asml.apa.wta.core.model;

import java.io.Serializable;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

/**
 * BaseSchema interface that all schema objects implement.
 *
 * @author  Lohithsai Yadala Chanchu
 * @since 1.0.0
 */
public interface BaseTraceObject extends Serializable {

  /**
   * Returns a hardcoded schema version.
   *
   * @return The associated config object
   * @author Lohithsai Yadala Chanchu
   * @since 1.0.0
   */
  default String getSchemaVersion() {
    return "1.0";
  }

  /**
   * All WTA objects that is stored in the parquet file need this method to convert the object to record for the writer.
   *
   * @param checker array indicating what column to ignore
   * @param schema schema for the output object
   * @return record of the object
   */
  GenericRecord convertToRecord(Boolean[] checker, Schema schema);
}
