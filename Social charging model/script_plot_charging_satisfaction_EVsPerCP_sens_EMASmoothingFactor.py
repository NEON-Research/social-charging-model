import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.lines import Line2D

# All behaviour scenarios
subselection = [
    {'b1': False, 'b2': False, 'b3': False, 'b4': True,
     'label': 'No behaviours', 'color': 'tab:blue'},
    {'b1': True,  'b2': False, 'b3': False, 'b4': True,
     'label': 'B1', 'color': 'tab:green'},
    {'b1': False, 'b2': True,  'b3': False, 'b4': True,
     'label': 'B2', 'color': 'tab:red'},
    {'b1': False, 'b2': False, 'b3': True,  'b4': True,
     'label': 'B3', 'color': 'tab:orange'},
    {'b1': True,  'b2': True,  'b3': False, 'b4': True,
     'label': 'B1 and B2', 'color': 'tab:cyan'},
    {'b1': True,  'b2': False, 'b3': True,  'b4': True,
     'label': 'B1 and B3', 'color': 'tab:olive'},
    {'b1': False, 'b2': True,  'b3': True,  'b4': True,
     'label': 'B2 and B3', 'color': 'tab:brown'},
    {'b1': True,  'b2': True,  'b3': True,  'b4': True,
     'label': 'All behaviours', 'color': 'tab:purple'},
]

# Three EMASmoothingFactor values to compare
ema_styles = [
    {'value': 0.1, 'linestyle': '--',  'ema_label': 'EMASmoothingFactor = 0.1'},
    {'value': 0.2, 'linestyle': '-', 'ema_label': 'EMASmoothingFactor = 0.2'},
    {'value': 0.3, 'linestyle': ':',  'ema_label': 'EMASmoothingFactor = 0.3'},
]

# Load data
excel_file = 'SCM_results_behaviours_sensEMASmoothingFactor.xlsx'
df = pd.read_excel(excel_file, sheet_name=0)

# --- Setup plot ---
fig, ax = plt.subplots(figsize=(3.3, 4.45))

for sel in subselection:
    for es in ema_styles:
        mask = (
            (df['b1'] == sel['b1']) &
            (df['b2'] == sel['b2']) &
            (df['b3'] == sel['b3']) &
            (df['b4'] == sel['b4']) &
            (df['EMASmoothingFactor'] == es['value'])
        )

        data = df[mask & (df['week'] >= 42)].copy()
        data = data.sort_values(['charge_points', 'EVsPerCP', 'week'])
        data['m_cs'] *= 100

        data_mean = (
            data.groupby('charge_points', as_index=False)
            .agg({'m_cs': 'mean', 'EVsPerCP': 'mean'})
        ).sort_values('EVsPerCP')

        if data_mean.empty:
            continue

        ax.plot(
            data_mean['EVsPerCP'],
            data_mean['m_cs'],
            color=sel['color'],
            linestyle=es['linestyle'],
            linewidth=2
        )

# --- Format plot ---
ax.set_title('Charging fulfillment ratio\n(sensitivity: EMA smoothing factor)', fontsize=9, pad=15)
ax.set_xlabel('EVs per CP', fontsize=8)
ax.tick_params(axis='both', labelsize=8)
ax.set_xlim(1, 15)
ax.set_xticks([5, 10, 15])

# --- Two-part legend: colors for scenarios, linestyles for EMA values ---
color_handles = [
    Line2D([0], [0], color=sel['color'], linewidth=2, linestyle='-', label=sel['label'])
    for sel in subselection
]
style_handles = [
    Line2D([0], [0], color='black', linewidth=2, linestyle=es['linestyle'], label=es['ema_label'])
    for es in ema_styles
]

legend = fig.legend(handles=color_handles + style_handles,
                    loc='upper center',
                    ncol=2,
                    frameon=False,
                    bbox_to_anchor=(0.5, 0.18),
                    fontsize=8)

fig.subplots_adjust(bottom=0.3, top=0.8)

# --- Save plot ---
fig.savefig('plot_charging_satisfaction_EVsPerCP_sens_EMASmoothingFactor.png', bbox_inches='tight', dpi=300)

plt.show()
