package com.asml.apa.wta.core;

import com.asml.apa.wta.core.io.JsonWriter;
import com.asml.apa.wta.core.io.OutputFile;
import com.asml.apa.wta.core.io.ParquetSchema;
import com.asml.apa.wta.core.io.ParquetWriter;
import com.asml.apa.wta.core.model.Resource;
import com.asml.apa.wta.core.model.ResourceStore;
import com.asml.apa.wta.core.model.Task;
import com.asml.apa.wta.core.model.TaskStore;
import com.asml.apa.wta.core.model.Workflow;
import com.asml.apa.wta.core.model.WorkflowStore;
import com.asml.apa.wta.core.model.Workload;
import java.io.IOException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Facade over the specific writers to persists all the WTA files.
 *
 * @author Atour Mousavi Gourabi
 * @since 1.0.0
 */
@Slf4j
public class WtaWriter {

  private final OutputFile file;
  private final String schemaVersion;

  /**
   * Sets up a WTA writer for the specified output path and version.
   *
   * @param path the output path to write to
   * @param version the version of files to write
   * @author Atour Mousavi Gourabi
   * @since 1.0.0
   */
  public WtaWriter(@NonNull OutputFile path, String version) {
    file = path;
    schemaVersion = version;
    setupDirectories(path, version);
  }

  /**
   * Writes a {@link Workload} to the corresponding JSON file.
   *
   * @param workload the workload to write
   * @author Atour Mousavi Gourabi
   * @since 1.0.0
   */
  public void write(Workload workload) {
    OutputFile path = file.resolve("workload").resolve(schemaVersion).resolve("generic_information.json");
    try (JsonWriter<Workload> workloadWriter = new JsonWriter<>(path)) {
      workloadWriter.write(workload);
    } catch (IOException e) {
      log.error("Could not write workload to file.");
    }
  }

  /**
   * Writes a {@link WorkflowStore} of {@link Workflow}s to the corresponding Parquet file.
   *
   * @param workflows the workflows to write
   * @author Atour Mousavi Gourabi
   * @since 1.0.0
   */
  public void write(WorkflowStore workflows) {
    ParquetSchema schema = new ParquetSchema(Workflow.class, workflows.getWorkflows(), "workflows");
    OutputFile path = file.resolve("workflows").resolve(schemaVersion).resolve("workflow.parquet");
    try (ParquetWriter<Workflow> workflowWriter = new ParquetWriter<>(path, schema)) {
      for (Workflow workflow : workflows.getWorkflows()) {
        workflowWriter.write(workflow);
      }
    } catch (IOException e) {
      log.error("Could not write workflows to file.");
    }
  }

  /**
   * Writes a {@link TaskStore} of {@link Task}s to the corresponding Parquet file.
   *
   * @param tasks the tasks to write
   * @author Atour Mousavi Gourabi
   * @since 1.0.0
   */
  public void write(TaskStore tasks) {
    ParquetSchema schema = new ParquetSchema(Task.class, tasks.getTasks(), "tasks");
    OutputFile path = file.resolve("tasks").resolve(schemaVersion).resolve("task.parquet");
    try (ParquetWriter<Task> taskWriter = new ParquetWriter<>(path, schema)) {
      for (Task task : tasks.getTasks()) {
        taskWriter.write(task);
      }
    } catch (IOException e) {
      log.error("Could not write tasks to file.");
    }
  }

  /**
   * Writes a {@link ResourceStore} of {@link Resource}s to the corresponding Parquet file.
   *
   * @param resources the resources to write
   * @author Atour Mousavi Gourabi
   * @since 1.0.0
   */
  public void write(ResourceStore resources) {
    ParquetSchema schema = new ParquetSchema(Resource.class, resources.getResources(), "resources");
    OutputFile path = file.resolve("resources").resolve(schemaVersion).resolve("resource.parquet");
    try (ParquetWriter<Resource> resourceWriter = new ParquetWriter<>(path, schema)) {
      for (Resource resource : resources.getResources()) {
        resourceWriter.write(resource);
      }
    } catch (IOException e) {
      log.error("Could not write resources to file.");
    }
  }

  /**
   * Prepares the system for writing.
   * Deletes old files in the output folder and initialises the directory structure.
   *
   * @author Atour Mousavi Gourabi
   * @since 1.0.0
   */
  private void setupDirectories(OutputFile path, String version) {
    try {
      path.resolve("workload").resolve(version).resolve(".temp").clearDirectory();
      path.resolve("workflows").resolve(version).resolve(".temp").clearDirectory();
      path.resolve("tasks").resolve(version).resolve(".temp").clearDirectory();
      path.resolve("resources").resolve(version).resolve(".temp").clearDirectory();
    } catch (IOException e) {
      log.error("Could not create directory structure for the output.");
    }
  }
}
