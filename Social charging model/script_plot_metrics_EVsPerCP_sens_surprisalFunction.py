import pandas as pd
import matplotlib.pyplot as plt

excel_file = 'SCM_results_behaviours_sensSurprisalFunction.xlsx'
df = pd.read_excel(excel_file, sheet_name=0)

print("Unique surprisalFunction values in data:", df['surprisalFunction'].unique())

# EVs per CP scenarios (subplots)
subselection = [
    {'b1': True, 'b2': True, 'b3': True, 'b4': True, 'EVsPerCP': 5,     'color': 'tab:blue',   'label': '5 EVs per CP'},
    {'b1': True, 'b2': True, 'b3': True, 'b4': True, 'EVsPerCP': 10,    'color': 'tab:orange', 'label': '10 EVs per CP'},
    {'b1': True, 'b2': True, 'b3': True, 'b4': True, 'EVsPerCP': 100/7, 'color': 'tab:green',  'label': '14.3 EVs per CP'},
]

# Metrics to plot
metrics = [
    {'met': 'pcp', 'label': 'Perceived CP pressure'},
    {'met': 'psi', 'label': 'Perceived social interdependence'},
    {'met': 'n1',  'label': 'Norm b1 (moving)'},
    {'met': 'n2',  'label': 'Norm b2 (requesting)'},
    {'met': 'n3',  'label': 'Norm b3 (notifying)'},
]

surprisal_functions = ['log', 'linear', 'quadratic']

width  = 15.92 / 2.52
height = width * (3 / 7)

for sf in surprisal_functions:
    fig, axes = plt.subplots(1, 3, figsize=(width, height))

    for idx, sel in enumerate(subselection):
        ax = axes[idx]

        mask = (
            (df['b1'] == sel['b1']) &
            (df['b2'] == sel['b2']) &
            (df['b3'] == sel['b3']) &
            (df['b4'] == sel['b4']) &
            (df['EVsPerCP'] == sel['EVsPerCP']) &
            (df['surprisalFunction'] == sf) &
            (df['week'] >= 1)
        )

        data = df[mask].copy()
        if data.empty:
            continue

        data = data.sort_values(['charge_points', 'week'])

        for met in metrics:
            abbr_metric = f"m_{met['met']}"
            if abbr_metric not in data.columns:
                continue

            ax.plot(
                data['week'],
                data[abbr_metric],
                label=met['label'],
                linewidth=1.8
            )

        ax.set_title(sel['label'], fontsize=8, pad=10)
        ax.set_xlabel('Week', fontsize=8)
        ax.set_ylim(0, 1)
        ax.tick_params(axis='both', labelsize=7)
        ax.set_xlim(data['week'].min(), data['week'].max())

    axes[0].set_ylabel('Metric value', fontsize=8)

    # Combined legend
    handles, labels = [], []
    for ax in axes.flat:
        h, l = ax.get_legend_handles_labels()
        for handle, label in zip(h, l):
            if label not in labels:
                handles.append(handle)
                labels.append(label)

    fig.legend(
        handles, labels,
        loc='lower center',
        ncol=3,
        frameon=False,
        bbox_to_anchor=(0.5, -0.05),
        fontsize=7
    )

    fig.suptitle(
        f"Learning in average behavioural characteristics\n(all-behaviours scenario, surprisalFunction = {sf})",
        fontsize=9
    )
    fig.subplots_adjust(bottom=0.24, top=0.76, wspace=0.3)

    fig.savefig(f'plot_metrics_EVsPerCP_surprisalFunction_{sf}.png', bbox_inches='tight', dpi=300)

plt.show()
