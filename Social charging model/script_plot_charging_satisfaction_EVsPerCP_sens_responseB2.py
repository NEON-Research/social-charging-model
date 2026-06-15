import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.lines import Line2D

# Scenarios that include B2
subselection = [
    {'b1': False, 'b2': True,  'b3': False, 'b4': True,
     'label': 'B2', 'color': 'tab:red'},
    {'b1': True,  'b2': True,  'b3': True,  'b4': True,
     'label': 'All behaviours', 'color': 'tab:purple'},
]

# Two randomMissFactorB2 values to compare
factor_styles = [
    {'value': 0.5, 'linestyle': ':',  'factor_label': '50% unavailable'},
    {'value': 0.25, 'linestyle': '--',  'factor_label': '25% unavailable'},
    {'value': 0.0, 'linestyle': '-', 'factor_label': '0% unavailable'},
]

# Load data
excel_file = 'SCM_results_behaviours_sensResponseB2.xlsx'
df = pd.read_excel(excel_file, sheet_name=0)

# --- Setup plot ---
fig, ax = plt.subplots(figsize=(3.3, 4.45))

for sel in subselection:
    for fs in factor_styles:
        mask = (
            (df['b1'] == sel['b1']) &
            (df['b2'] == sel['b2']) &
            (df['b3'] == sel['b3']) &
            (df['b4'] == sel['b4']) &
            (df['randomMissFactorB2'] == fs['value'])
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
            linestyle=fs['linestyle'],
            linewidth=2
        )

# --- Format plot ---
ax.set_title('Charging fulfillment ratio\n(sensitivity: Unsuccessful response factor B2)', fontsize=9, pad=15)
ax.set_xlabel('EVs per CP', fontsize=8)
ax.tick_params(axis='both', labelsize=8)
ax.set_xlim(1, 15)
ax.set_xticks([5, 10, 15])

# --- Two-part legend: colors for scenarios, linestyles for factor values ---
color_handles = [
    Line2D([0], [0], color=sel['color'], linewidth=2, linestyle='-', label=sel['label'])
    for sel in subselection
]
style_handles = [
    Line2D([0], [0], color='black', linewidth=2, linestyle=fs['linestyle'], label=fs['factor_label'])
    for fs in factor_styles
]

fig.legend(handles=color_handles + style_handles,
           loc='lower center',
           ncol=2,
           frameon=False,
           bbox_to_anchor=(0.5, -0.05),
           fontsize=8)

fig.subplots_adjust(bottom=0.3, top=0.8)

# --- Save plot ---
fig.savefig('plot_charging_satisfaction_EVsPerCP_sens_responseB2.png', bbox_inches='tight', dpi=300)

plt.show()
