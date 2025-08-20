import pandas as pd
import matplotlib.pyplot as plt


# Set the Excel file name
excel_file = 'SCM_export_results.xlsx'

# Read the first sheet (default)
df = pd.read_excel(excel_file)
# Read the second sheet for out of model charge, left without charging, left while charging
df2 = pd.read_excel(excel_file, sheet_name=1)

# Create two rows of three subplots: first row for success rate (sr), second for average probability (ar)
behaviors = ['b1', 'b2', 'b3']
unique_scenarios = df['scenario'].unique()


# Add a third row for the new metrics
# A4 width is about 8.27 inches; set width to 8.3 and height to 9 for 3 rows
fig, axes = plt.subplots(3, 3, figsize=(7.2, 8), sharey='row')



# First row: success rate (sr)
for idx, behavior in enumerate(behaviors):
    ax = axes[0, idx]
    mean_col = f'm_sr_{behavior}'
    lower_col = f'l_sr_{behavior}'
    upper_col = f'u_sr_{behavior}'
    for scenario in unique_scenarios:
        data = df[df['scenario'] == scenario]
        ax.plot(data['day'], data[mean_col], label=f'Scenario {scenario} Mean')
        ax.fill_between(data['day'], data[lower_col], data[upper_col], alpha=0.2)
    ax.set_title(f'Success Rate: Behavior {behavior}', fontsize=10)
    # No x-axis label for first row
    if idx == 0:
        ax.set_ylabel('Success Rate')
    ax.tick_params(axis='both', labelsize=8)


# Second row: average probability (ar)
for idx, behavior in enumerate(behaviors):
    ax = axes[1, idx]
    mean_col = f'm_ap_{behavior}'
    lower_col = f'l_ap_{behavior}'
    upper_col = f'u_ap_{behavior}'
    for scenario in unique_scenarios:
        data = df[df['scenario'] == scenario]
        ax.plot(data['day'], data[mean_col], label=f'Scenario {scenario} Mean')
        ax.fill_between(data['day'], data[lower_col], data[upper_col], alpha=0.2)
    ax.set_title(f'Probability: Behavior {behavior}', fontsize=10)
    # No x-axis label for second row
    if idx == 0:
        ax.set_ylabel('Probability')
    ax.tick_params(axis='both', labelsize=8)

# Third row: out of model charge (oomc), left without charging (lwc), left while charging (luc)
metrics = [
    ('oomc', 'Out of Model Charge (kWh/day)'),
    ('lwc', 'Left Without Charging (#/day)'),
    ('luc', 'Left While Charging (#/day)')
]
unique_scenarios2 = df2['scenario'].unique()
for idx, (metric, title) in enumerate(metrics):
    ax = axes[2, idx]
    mean_col = f'm_{metric}'
    lower_col = f'l_{metric}'
    upper_col = f'u_{metric}'
    for scenario in unique_scenarios2:
        data = df2[df2['scenario'] == scenario]
        ax.plot(data['day'], data[mean_col], label=f'Scenario {scenario} Mean')
        ax.fill_between(data['day'], data[lower_col], data[upper_col], alpha=0.2)
    ax.set_title(title, fontsize=10)
    # Only bottom row gets x-axis label
    ax.set_xlabel('day', fontsize=10)
    if idx == 0:
        ax.set_ylabel('Value')
    ax.tick_params(axis='both', labelsize=8)



# Add a single legend at the bottom spanning all columns using fig.legend
handles, labels = [], []
for ax in axes.flat:
    h, l = ax.get_legend_handles_labels()
    for handle, label in zip(h, l):
        if label not in labels:
            handles.append(handle)
            labels.append(label)
if handles:
    fig.legend(handles, labels, loc='lower center', ncol=len(labels), frameon=False, bbox_to_anchor=(0.5, -0.03))

plt.suptitle('Scenario results for different behaviors and metrics', fontsize=14)
 # Remove supxlabel since we now use per-bottom-row xlabels
plt.tight_layout(rect=[0, 0.03, 1, 0.95])
# Save the figure as PDF and PNG for full-size viewing
fig.savefig('plot_scenarios_output.pdf', bbox_inches='tight')
fig.savefig('plot_scenarios_output.png', bbox_inches='tight', dpi=300)
plt.show()


