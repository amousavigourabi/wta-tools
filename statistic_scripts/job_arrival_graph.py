import math
import os

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from sortedcontainers import SortedDict


class JobArrivalGraph(object):

    def __init__(self, workload_name, df, image_folder_location):
        self.workload_name = workload_name
        self.df = df
        self.folder = image_folder_location

    def generate_content(self):
        plot_location = self.generate_graphs()

        return None, plot_location

    def generate_graphs(self, show=False):
        plt.figure()
        granularity_order = [
            "Second",
            "Minute",
            "Hour",
            "Day"
        ]

        granularity_lambdas = {
            "Second": lambda x: x,
            "Minute": lambda x: x / 60,
            "Hour": lambda x: x / (60 * 60),
            "Day": lambda x: x / (60 * 60 * 24),
        }

        plot_count = 0
        for granularity in granularity_order:
            job_arrivals = SortedDict()
            for workflow in self.df.itertuples():
                submit_time = int(workflow.ts_submit)

                submit_time = granularity_lambdas[granularity](submit_time)

                if submit_time not in job_arrivals:
                    job_arrivals[submit_time] = 0

                job_arrivals[submit_time] += 1

            ax = plt.subplot2grid((2, 2), (int(math.floor(plot_count / 2)), (plot_count % 2)))

            if max(job_arrivals.keys()) >= 1:
                ax.plot(job_arrivals.keys(), job_arrivals.values(), color="black", linewidth=1.0)
                ax.grid(True)
            else:
                ax.text(0.5, 0.5, 'Not available;\nTrace too small.', horizontalalignment='center',
                verticalalignment = 'center', transform = ax.transAxes, fontsize=16)
                ax.grid(False)

            ax.locator_params(nbins=3, axis='y')
            ax.set_xlim(0)
            ax.set_ylim(0)
            ax.margins(0.05)

            ax.set_xlabel('Time ({0})'.format(granularity.lower()), fontsize=16)
            ax.set_ylabel('# Jobs', fontsize=16)
            plot_count += 1

            # Rotates and right aligns the x labels, and moves the bottom of the
            # axes up to make room for them
            # fig.autofmt_xdate()

        plt.tight_layout()

        filename = "job_arrival_{0}".format(self.workload_name)
        plt.savefig(os.path.join(self.folder, filename), dpi=200)
        if show:
            plt.show()

        return filename
