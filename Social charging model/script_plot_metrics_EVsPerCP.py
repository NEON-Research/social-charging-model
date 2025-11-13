import pandas as pd
import matplotlib.pyplot as plt

excel_file = 'SCM_results_behaviours.xlsx'
df = pd.read_excel(excel_file, sheet_name=1)

# Define the scenarios for each subplot
subselection = [
    {'b1': True, 'b2': True, 'b3': True, 'b4': False, 'EVsPerCP': 5,  'color': 'tab:blue',  'label': 'All behaviors (5 EVs/CP)'},
    {'b1': True, 'b2': True, 'b3': True, 'b4': False, 'EVsPerCP': 10, 'color': 'tab:orange','label': 'All behaviors (10 EVs/CP)'},
    {'b1': True, 'b2': True, 'b3': True, 'b4': False, 'EVsPerCP': 14, 'color': 'tab:green', 'label': 'All behaviors (15 EVs/CP)'}
]

# Metrics to plot as lines
metrics = [
    {'met': 'pcp', 'label': 'Perceived CP pressure'},
    # {'met': 'rc', 'label': 'Reputational concern'},
    {'met': 'psi', 'label': 'Perceived social interdependence'},
    {'met': 'n1', 'label': 'Norm behavior 1'},
    {'met': 'n2', 'label': 'Norm behavior 2'},
    {'met': 'n3', 'label': 'Norm behavior 3'}
]

# --- Create subplots ---
width = 15.92 / 2.52 # width word cm to inch
height = width * (3 / 7)  # maintain aspect ratio
fig, axes = plt.subplots(1, 3, figsize=(width, height))

for idx, sel in enumerate(subselection):
    ax = axes[idx]

    # Common mask for all metrics
    mask = (
        (df['b1'] == sel['b1']) &
        (df['b2'] == sel['b2']) &
        (df['b3'] == sel['b3']) &
        (df['b4'] == sel['b4']) &
        (df['EVsPerCP'] == sel['EVsPerCP']) &
        (df['week'] >= 1)
    )

    data = df[mask].copy()
    if data.empty:
        continue

    data = data.sort_values(['charge_points', 'week'])

    # --- Plot each metric ---
    for met in metrics:
        abbr_metric = f"m_{met['met']}"
        if abbr_metric not in data.columns:
            continue
        
        #data_metric = data[abbr_metric]
        ax.plot(
            data['week'],
            data[abbr_metric],
            label=met['label'],
            linewidth=1.8
        )

    # --- Format subplot ---
    ax.set_title(sel['label'], fontsize=8, pad=10)
    ax.set_xlabel('Week', fontsize=8)
    ax.set_ylim(0, 1)
    ax.tick_params(axis='both', labelsize=7)
    ax.set_xlim(data['week'].min(), data['week'].max())

axes[0].set_ylabel('Metric value', fontsize=8)

# --- Combined legend ---
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

# Layout
fig.suptitle("Learning in average behavioral characteristics", fontsize=9)
fig.subplots_adjust(bottom=0.24, top=0.8, wspace=0.3)

# Save and show
fig.savefig('plot_metrics_EVsPerCP.png', bbox_inches='tight', dpi=300)
plt.show()
