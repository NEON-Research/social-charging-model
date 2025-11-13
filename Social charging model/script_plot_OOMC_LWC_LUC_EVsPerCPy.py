import pandas as pd
import matplotlib.pyplot as plt

# Define the behavior scenarios
subselection1 = [
    {'b1': False, 'b2': False, 'b3': False, 'b4': False,
     'label': 'No behaviors', 'color': 'tab:blue', 'linestyle': '-'},
    {'b1': True,  'b2': False, 'b3': False, 'b4': False,
     'label': 'Behavior 1', 'color': 'tab:green', 'linestyle': '-'},
    {'b1': False, 'b2': True,  'b3': False, 'b4': False,
     'label': 'Behavior 2', 'color': 'tab:red', 'linestyle': '-'},
    {'b1': False, 'b2': False, 'b3': True,  'b4': False,
     'label': 'Behavior 3', 'color': 'tab:orange', 'linestyle': '-'},
    {'b1': True,  'b2': True,  'b3': False, 'b4': False,
     'label': 'Behavior 1 and 2', 'color': 'tab:cyan', 'linestyle': '--'},
    {'b1': True,  'b2': False, 'b3': True,  'b4': False,
     'label': 'Behavior 1 and 3', 'color': 'tab:olive', 'linestyle': '--'},
    {'b1': False, 'b2': True,  'b3': True,  'b4': False,
     'label': 'Behavior 2 and 3', 'color': 'tab:brown', 'linestyle': '--'},
    {'b1': True,  'b2': True,  'b3': True,  'b4': False,
     'label': 'All social behaviors', 'color': 'tab:purple', 'linestyle': '-'}
]

# Load data
excel_file = 'SCM_results_behaviours.xlsx'
df = pd.read_excel(excel_file, sheet_name=1)

# --- Create subplots ---
width = 15.92 / 2.52  # width cm → inch
height = width * (3 / 7)  # maintain aspect ratio
fig, axes = plt.subplots(1, 3, figsize=(width, height))

# --- Define metrics ---
metrics = [
    {'abbr': 'm_oomc', 'label': 'Out of model charge'},
    {'abbr': 'm_lwc', 'label': 'Left while charging'},
    {'abbr': 'm_luc', 'label': 'Left without charging'}
]

# --- Plot for each metric ---
for i, (ax, metric) in enumerate(zip(axes, metrics)):
    for sel in subselection1:
        mask = (
            (df['b1'] == sel['b1']) &
            (df['b2'] == sel['b2']) &
            (df['b3'] == sel['b3']) &
            (df['b4'] == sel['b4'])
        )

        data = df[mask & (df['week'] >= 42) & (df['EVsPerCP'] <= 15)].copy()
        data = data.sort_values(['charge_points', 'EVsPerCP', 'week'])

        if data.empty:
            continue

        # Convert to percentage
        #data[metric['abbr']] *= 100

        # Aggregate mean by charge_points
        data_mean = (
            data[(data['week'] >= 43) & (data['week'] <= 52)]
            .groupby('EVsPerCP', as_index=False)
            .agg({metric['abbr']: 'mean'})
        )


        # Only add label for the first subplot
        label = sel['label'] if i == 0 else None

        ax.plot(
            data_mean['EVsPerCP'],
            data_mean[metric['abbr']],
            label=label,
            color=sel['color'],
            linestyle=sel['linestyle'],
            linewidth=2
        )

    # --- Format subplot ---
    ax.set_title(metric['label'], fontsize=9, pad=10)
    ax.set_xlabel('EVs per CP', fontsize=8)
    ax.tick_params(axis='both', labelsize=8)
    ax.set_xlim(1, 15)
    ax.set_xticks([5, 10, 15])

# --- Legend and layout ---
fig.legend(
    loc='lower center',
    ncol=min(len(subselection1), 4),
    frameon=False,
    bbox_to_anchor=(0.5, -0.05),
    fontsize=8
)

fig.subplots_adjust(bottom=0.2, wspace=0.25)
plt.show()

