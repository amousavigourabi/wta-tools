import os
from collections import OrderedDict

import pyarrow as pa

from pyspark.sql.types import *


class TaskState(object):
    _version = "1.0"

    def __init__(self, ts_start, ts_end, workflow_id, task_id, resource_id, cpu_rate=-1, canonical_memory_usage=-1,
                 assigned_memory=-1, minimum_memory_usage=-1, maximum_memory_usage=-1, disk_io_time=-1,
                 maximum_disk_bandwidth=-1, local_disk_space_usage=-1, maximum_cpu_rate=-1,
                 maximum_disk_io_time=-1, sample_rate=-1, sample_portion=-1, sampled_cpu_usage=-1,
                 network_io_time=-1, maximum_network_bandwidth=-1, network_in=-1, network_out=-1):
        self.ts_start = ts_start  # Start of the state measurement period
        self.ts_end = ts_end  # End of the state measurement period
        self.workflow_id = workflow_id  # The workflow ID the task belong to
        self.task_id = task_id  # The task ID
        self.resource_id = resource_id  # The resource ID the task state was snapped on
        self.cpu_rate = cpu_rate  # Mean CPU usage rate
        self.canonical_memory_usage = canonical_memory_usage  # The number of user accessible pages, including page cache but excluding some pages marked as stale.
        self.assigned_memory = assigned_memory  # Total amount of available memory
        self.minimum_memory_usage = minimum_memory_usage  # minimum memory usage observed during the interval
        self.maximum_memory_usage = maximum_memory_usage  # maximal memory usage observed during the interval
        self.disk_io_time = disk_io_time  # Total time spent on disk_io in milliseconds
        self.maximum_disk_bandwidth = maximum_disk_bandwidth  # The maximum disk bandwidth used during the interval
        self.local_disk_space_usage = local_disk_space_usage  # Amount of local disk space used, either fraction or in MB
        self.maximum_cpu_rate = maximum_cpu_rate  # Maximum cpu rate (as a fraction) during this interval
        self.maximum_disk_io_time = maximum_disk_io_time  # Longest segment of consecutive disk io
        self.sample_rate = sample_rate  # Amount of samples taken per second
        self.sample_portion = sample_portion  # The ratio between the number of expected samples to the number of observed samples
        self.sampled_cpu_usage = sampled_cpu_usage  # Mean CPU usage durong a random sample in the measurement period
        self.network_io_time = network_io_time  # Total time spent on network io in milliseconds
        self.maximum_network_bandwidth = maximum_network_bandwidth  # Maxmimum network bandwidth used during the interval
        self.network_in = network_in # Bytes transferred in
        self.network_out = network_out # Bytes transferred out

    def get_json_dict(self):
        return {
            "ts_start": self.ts_start,
            "ts_end": self.ts_end,
            "workflow_id": self.workflow_id,
            "task_id": self.task_id,
            "resource_id": self.resource_id,
            "cpu_rate": self.cpu_rate,
            "canonical_memory_usage": self.canonical_memory_usage,
            "assigned_memory": self.assigned_memory,
            "minimum_memory_usage": self.minimum_memory_usage,
            "maximum_memory_usage": self.maximum_memory_usage,
            "disk_io_time": self.disk_io_time,
            "maximum_disk_bandwidth": self.maximum_disk_bandwidth,
            "local_disk_space_usage": self.local_disk_space_usage,
            "maximum_cpu_rate": self.maximum_cpu_rate,
            "maximum_disk_io_time": self.maximum_disk_io_time,
            "sample_rate": self.sample_rate,
            "sample_portion": self.sample_portion,
            "sampled_cpu_usage": self.sampled_cpu_usage,
            "network_io_time": self.network_io_time,
            "maximum_network_bandwidth": self.maximum_network_bandwidth,
            "network_in": self.network_in,
            "network_out": self.network_out,
            "version": self._version,
        }

    @staticmethod
    def get_spark_type():
        type_info = [
            StructField("ts_start", LongType(), False),
            StructField("ts_end", LongType(), False),
            StructField("workflow_id", LongType(), False),
            StructField("task_id", LongType(), False),
            StructField("resource_id", LongType(), False),
            StructField("cpu_rate", DoubleType(), False),
            StructField("canonical_memory_usage", DoubleType(), False),
            StructField("assigned_memory", DoubleType(), False),
            StructField("minimum_memory_usage", DoubleType(), False),
            StructField("maximum_memory_usage", DoubleType(), False),
            StructField("disk_io_time", LongType(), False),
            StructField("maximum_disk_bandwidth", DoubleType(), False),
            StructField("local_disk_space_usage", DoubleType(), False),
            StructField("maximum_cpu_rate", DoubleType(), False),
            StructField("maximum_disk_io_time", LongType(), False),
            StructField("sample_rate", DoubleType(), False),
            StructField("sample_portion", DoubleType(), False),
            StructField("sampled_cpu_usage", DoubleType(), False),
            StructField("network_io_time", DoubleType(), False),
            StructField("maximum_network_bandwidth", DoubleType(), False),
            StructField("network_in", LongType(), False),
            StructField("network_out", LongType(), False),
            StructField("version", StringType(), False),
        ]

        sorted_type_info = sorted(type_info, key=lambda x: x.name)
        return StructType(sorted_type_info)

    @staticmethod
    def get_parquet_meta_dict():
        type_info = {
            "ts_start": int,
            "ts_end": int,
            "workflow_id": int,
            "task_id": int,
            "resource_id": int,
            "cpu_rate": float,
            "canonical_memory_usage": float,
            "assigned_memory": float,
            "minimum_memory_usage": float,
            "maximum_memory_usage": float,
            "disk_io_time": int,
            "maximum_disk_bandwidth": float,
            "local_disk_space_usage": float,
            "maximum_cpu_rate": float,
            "maximum_disk_io_time": int,
            "sample_rate": float,
            "sample_portion": float,
            "sampled_cpu_usage": float,
            "network_io_time": float,
            "maximum_network_bandwidth": float,
            "network_in": int,
            "network_out": int,
            "version": TaskState._version,
        }

        ordered_dict = OrderedDict(sorted(type_info.items(), key=lambda t: t[0]))

        return ordered_dict

    def get_parquet_dict(self):
        simple_dict = {
            "ts_start": int(self.ts_start),
            "ts_end": int(self.ts_end),
            "workflow_id": int(self.workflow_id),
            "task_id": int(self.task_id),
            "resource_id": int(self.resource_id),
            "cpu_rate": float(self.cpu_rate),
            "canonical_memory_usage": float(self.canonical_memory_usage),
            "assigned_memory": float(self.assigned_memory),
            "minimum_memory_usage": float(self.minimum_memory_usage),
            "maximum_memory_usage": float(self.maximum_memory_usage),
            "disk_io_time": int(self.disk_io_time),
            "maximum_disk_bandwidth": float(self.maximum_disk_bandwidth),
            "local_disk_space_usage": float(self.local_disk_space_usage),
            "maximum_cpu_rate": float(self.maximum_cpu_rate),
            "maximum_disk_io_time": int(self.maximum_disk_io_time),
            "sample_rate": float(self.sample_rate),
            "sample_portion": float(self.sample_portion),
            "sampled_cpu_usage": float(self.sampled_cpu_usage),
            "network_io_time": float(self.network_io_time),
            "maximum_network_bandwidth": float(self.maximum_network_bandwidth),
            "network_in": int(self.network_in),
            "network_out": int(self.network_out),
            "version": self._version,
        }

        ordered_dict = OrderedDict(sorted(simple_dict.items(), key=lambda t: t[0]))

        return ordered_dict

    @staticmethod
    def get_pyarrow_schema():
        fields = [
            pa.field("ts_start", pa.int64()),
            pa.field("ts_end", pa.int64()),
            pa.field("workflow_id", pa.int64()),
            pa.field("task_id", pa.int64()),
            pa.field("resource_id", pa.int64()),
            pa.field("cpu_rate", pa.float64()),
            pa.field("canonical_memory_usage", pa.float64()),
            pa.field("assigned_memory", pa.float64()),
            pa.field("minimum_memory_usage", pa.float64()),
            pa.field("maximum_memory_usage", pa.float64()),
            pa.field("disk_io_time", pa.int64()),
            pa.field("maximum_disk_bandwidth", pa.float64()),
            pa.field("local_disk_space_usage", pa.float64()),
            pa.field("maximum_cpu_rate", pa.float64()),
            pa.field("maximum_disk_io_time", pa.int64()),
            pa.field("sample_rate", pa.float64()),
            pa.field("sample_portion", pa.float64()),
            pa.field("sampled_cpu_usage", pa.float64()),
            pa.field("network_io_time", pa.float64()),
            pa.field("maximum_network_bandwidth", pa.float64()),
            pa.field("network_in", pa.int64()),
            pa.field("network_out", pa.int64()),
            pa.field("version", pa.string()),
        ]

        return pa.schema(fields)

    @staticmethod
    def versioned_dir_name():
        return "schema-{}".format(TaskState._version)

    @staticmethod
    def output_path():
        return os.path.join("task-states", TaskState.versioned_dir_name())
