package com.asml.apa.wta.core.io;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import lombok.extern.slf4j.Slf4j;
import org.apache.parquet.io.OutputFile;
import org.apache.parquet.io.PositionOutputStream;

/**
 * Disk Parquet output file to wrap {@link Path}s for the {@link org.apache.parquet.hadoop.ParquetWriter}.
 *
 * @author Atour Mousavi Gourabi
 * @since 1.0.0
 */
@Slf4j
public class DiskParquetOutputFile implements OutputFile {

  private class LocalPositionOutputStream extends PositionOutputStream {

    private final BufferedOutputStream stream;
    private long pos = 0;

    LocalPositionOutputStream(int buffer, StandardOpenOption... openOption) throws IOException {
      stream = new BufferedOutputStream(Files.newOutputStream(path, openOption), buffer);
    }

    /**
     * Get current position in the {@link BufferedOutputStream}.
     *
     * @return the current position
     * @author Atour Mousavi Gourabi
     * @since 1.0.0
     */
    @Override
    public long getPos() {
      return pos;
    }

    /**
     * Writes a byte from {@code data}.
     *
     * @see BufferedOutputStream#write(int)
     *
     * @param data the {@code byte}
     * @throws IOException when something goes wrong during I/O
     * @author Atour Mousavi Gourabi
     * @since 1.0.0
     */
    @Override
    public void write(int data) throws IOException {
      pos++;
      stream.write(data);
    }

    /**
     * Writes the bytes from {@code data}.
     *
     * @see BufferedOutputStream#write(byte[])
     *
     * @param data the data
     * @throws IOException when something goes wrong during I/O
     * @author Atour Mousavi Gourabi
     * @since 1.0.0
     */
    @Override
    public void write(byte[] data) throws IOException {
      pos += data.length;
      stream.write(data);
    }

    /**
     * Writes {@code len} bytes from {@code data} starting at {@code off}.
     *
     * @see BufferedOutputStream#write(byte[], int, int)
     *
     * @param data the data
     * @param off the start offset in the data
     * @param len the number of bytes to write
     * @throws IOException when something goes wrong during I/O
     * @author Atour Mousavi Gourabi
     * @since 1.0.0
     */
    @Override
    public void write(byte[] data, int off, int len) throws IOException {
      pos += len;
      stream.write(data, off, len);
    }

    /**
     * Flushes the {@link DiskParquetOutputFile}.
     *
     * @throws IOException when something goes wrong during I/O
     * @author Atour Mousavi Gourabi
     * @since 1.0.0
     */
    @Override
    public void flush() throws IOException {
      stream.flush();
    }

    /**
     * Closes the {@link DiskParquetOutputFile}.
     *
     * @throws IOException when something goes wrong during I/O
     * @author Atour Mousavi Gourabi
     * @since 1.0.0
     */
    @Override
    public void close() throws IOException {
      stream.close();
    }
  }

  private final Path path;

  public DiskParquetOutputFile(Path file) {
    path = file;
  }

  /**
   * Creates a {@link PositionOutputStream} for the wrapped {@link Path}.
   *
   * @param buffer buffer hint, should not exceed {@link Integer#MAX_VALUE}
   * @return the created {@link PositionOutputStream}
   * @throws IOException when something goes wrong during I/O
   * @author Atour Mousavi Gourabi
   * @since 1.0.0
   */
  @Override
  public PositionOutputStream create(long buffer) throws IOException {
    return new LocalPositionOutputStream((int) buffer, StandardOpenOption.CREATE_NEW);
  }

  /**
   * Creates a {@link PositionOutputStream} for the wrapped {@link Path}.
   * Overwrites files when they are already present.
   *
   * @param buffer buffer hint
   * @return the created {@link PositionOutputStream}
   * @throws IOException when something goes wrong during I/O
   * @author Atour Mousavi Gourabi
   * @since 1.0.0
   */
  @Override
  public PositionOutputStream createOrOverwrite(long buffer) throws IOException {
    return new LocalPositionOutputStream(
        (int) buffer, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
  }

  /**
   * Checks whether the output file supports block size.
   *
   * @return {@code true}
   * @author Atour Mousavi Gourabi
   * @since 1.0.0
   */
  @Override
  public boolean supportsBlockSize() {
    return true;
  }

  /**
   * Returns the default block size.
   *
   * @return {@code 512}, the default value for {@link BufferedOutputStream}
   * @author Atour Mousavi Gourabi
   * @since 1.0.0
   */
  @Override
  public long defaultBlockSize() {
    return 512;
  }

  /**
   * Returns the path of the {@link com.asml.apa.wta.core.io.OutputFile} as a {@link String}.
   *
   * @return the path of this {@link com.asml.apa.wta.core.io.OutputFile} as a {@link String}
   * @author Atour Mousavi Gourabi
   * @since 1.0.0
   */
  @Override
  public String getPath() {
    return path.toString();
  }
}
