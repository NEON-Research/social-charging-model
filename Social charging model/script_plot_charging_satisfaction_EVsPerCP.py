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

# --- Setup plot ---
fig, ax = plt.subplots(figsize=(3.3, 3.85))

# --- Plot charging satisfaction for each scenario ---
for sel in subselection1:
    mask = (
        (df['b1'] == sel['b1']) &
        (df['b2'] == sel['b2']) &
        (df['b3'] == sel['b3']) &
        (df['b4'] == sel['b4'])
    )

    data = df[mask & (df['week'] >= 42) & (df['EVsPerCP'] <= 15)].copy()
    data = data.sort_values(['charge_points', 'EVsPerCP', 'week'])

    # Convert to percentage
    data['m_cs'] *= 100

    # Aggregate mean by charge_points
    data_mean = (
        data.groupby('charge_points', as_index=False)
        .agg({'m_cs': 'mean', 'EVsPerCP': 'mean'})
    ).sort_values('EVsPerCP')

    if data_mean.empty:
        continue

    ax.plot(
        data_mean['EVsPerCP'],
        data_mean['m_cs'],
        label=sel['label'],
        color=sel['color'],
        linestyle=sel['linestyle'],
        linewidth=2
    )

# --- Format plot ---
ax.set_title('Charging fulfillment ratio\n(% of required charging sessions fulfilled)', fontsize=9, pad=15)
ax.set_xlabel('EVs per CP', fontsize=8)
#ax.set_ylabel('Charging Satisfaction (%)', fontsize=9)
ax.tick_params(axis='both', labelsize=8)
ax.set_xlim(1, 15)
ax.set_xticks([5, 10, 15])


# --- Legend and layout ---
fig.legend(loc='lower center',
           ncol=min(len(subselection1), 4),
           frameon=False,
           bbox_to_anchor=(0.5, -0.05),
           fontsize=8)

fig.subplots_adjust(bottom=0.2)

# --- Save plot ---
fig.savefig('plot_charging_satisfaction_EVsPerCP.pdf', bbox_inches='tight')
fig.savefig('plot_charging_satisfaction_EVsPerCP.png', bbox_inches='tight', dpi=300)




plt.show()
