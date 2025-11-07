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

# --- Create subplots: one per EVsPerCP value ---
EV_values = [5, 10, 14]
fig, axes = plt.subplots(1, len(EV_values), figsize=(10, 4), sharey=True)

for ax_i, (ax, ev_value) in enumerate(zip(axes, EV_values)):
    for sel in subselection1:
        mask = (
            (df['b1'] == sel['b1']) &
            (df['b2'] == sel['b2']) &
            (df['b3'] == sel['b3']) &
            (df['b4'] == sel['b4'])
        )

        data = df[mask & (df['EVsPerCP'] == ev_value) & (df['week'] >= 3)].copy()
        data = data.sort_values(['charge_points', 'EVsPerCP', 'week'])

        if data.empty:
            continue

        # Convert to percentage
        data['m_cs'] *= 100
        data['m_cs_rollmean'] = data['m_cs'].rolling(window=10, min_periods=1).mean()

        # Only add label for the first subplot
        label = sel['label'] if ax_i == 0 else None

        ax.plot(
            data['week'],
            data['m_cs_rollmean'],
            label=label,
            color=sel['color'],
            linestyle=sel['linestyle'],
            linewidth=2
        )

        title_value = ev_value
        if ev_value == 14:
            title_value += 1
        
    ax.set_title(f'{title_value} EVs per CP', fontsize=9, pad=10)
    ax.set_xlabel('Week', fontsize=8)
    ax.tick_params(axis='both', labelsize=8)

# Shared labels and legend
axes[0].set_ylabel('Charging Satisfaction (%)', fontsize=9)
fig.suptitle('Charging fulfillment ratio\n(% of required charging sessions fulfilled)', fontsize=10, y=1.02)

# --- Legend and layout ---
fig.legend(loc='lower center',
           ncol=4,
           frameon=False,
           bbox_to_anchor=(0.5, -0.05),
           fontsize=8)

fig.subplots_adjust(bottom=0.2)

# --- Save plot ---
fig.savefig('plot_charging_satisfaction_perWeek_threeSubplots.pdf', bbox_inches='tight')
fig.savefig('plot_charging_satisfaction_perWeek_threeSubplots.png', bbox_inches='tight', dpi=300)




plt.show()
