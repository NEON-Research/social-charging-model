import pandas as pd
import matplotlib.pyplot as plt

# Load data
excel_file = 'SCM_results_behaviours.xlsx'
df = pd.read_excel(excel_file, sheet_name=0)

# Define scenario groups
scenarios_group1 = [
    {'b1': False, 'b2': False, 'b3': False, 'b4': False, 'label': 'No behaviors', 'color': 'tab:blue'},
    {'b1': True,  'b2': True,  'b3': True,  'b4': False, 'label': 'All social behaviors', 'color': 'tab:purple'}
]

scenarios_group2 = [
    {'b1': True,  'b2': False, 'b3': False, 'b4': False, 'label': 'Behavior 1', 'color': 'tab:green'},
    {'b1': False, 'b2': True,  'b3': False, 'b4': False, 'label': 'Behavior 2', 'color': 'tab:red'},
    {'b1': False, 'b2': False, 'b3': True,  'b4': False, 'label': 'Behavior 3', 'color': 'tab:orange'}
]

scenarios_group3 = [
    {'b1': True,  'b2': True,  'b3': False, 'b4': False, 'label': 'Behavior 1 and 2', 'color': 'tab:cyan'},
    {'b1': True,  'b2': False, 'b3': True,  'b4': False, 'label': 'Behavior 1 and 3', 'color': 'tab:olive'},
    {'b1': False, 'b2': True,  'b3': True,  'b4': False, 'label': 'Behavior 2 and 3', 'color': 'tab:brown'}
]

width = 15.92 / 2.52 # width word cm to inch
height = width * (3 / 7)  # maintain aspect ratio
fig, axes = plt.subplots(1, 3, figsize=(width, height), sharey=True )

def plot_scenarios(ax, scenarios):
    for sel in scenarios:
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
        data['l_cs'] *= 100
        data['u_cs'] *= 100

        # Aggregate by charge_points
        data_mean = (
            data.groupby('charge_points', as_index=False)
            .agg({'m_cs': 'mean', 'l_cs': 'mean', 'u_cs': 'mean', 'EVsPerCP': 'mean'})
        ).sort_values('EVsPerCP')

        if data_mean.empty:
            continue

        # Plot mean line
        ax.plot(data_mean['EVsPerCP'], data_mean['m_cs'],
                label=sel['label'], color=sel['color'], linewidth=2)

        # Confidence interval shading
        ax.fill_between(data_mean['EVsPerCP'], data_mean['l_cs'], data_mean['u_cs'],
                        color=sel['color'], alpha=0.2)

    ax.set_xlim(1, 15)
    ax.set_xticks([5, 10, 15])
    ax.set_yticks([30, 40, 50, 60, 70, 80, 90, 100])
    ax.set_xlabel('EVs per CP', fontsize=8)
    ax.tick_params(axis='both', labelsize=8)

# Plot each group
plot_scenarios(axes[0], scenarios_group1)
axes[0].set_title('No and All behaviors', fontsize=8)

plot_scenarios(axes[1], scenarios_group2)
axes[1].set_title('Individual behaviors', fontsize=8)

plot_scenarios(axes[2], scenarios_group3)
axes[2].set_title('Combination behaviors', fontsize=8)

# Shared y-label
axes[0].set_ylabel('Charging fulfillment ratio (%)', fontsize=8)

# Single legend below all plots
fig.legend(loc='lower center', ncol=4, frameon=False, bbox_to_anchor=(0.5, -0.05), fontsize=8)
fig.subplots_adjust(bottom=0.25, wspace=0.3)

# Save figure
fig.savefig('plot_charging_satisfaction_behaviors_confidence.png', bbox_inches='tight', dpi=300)