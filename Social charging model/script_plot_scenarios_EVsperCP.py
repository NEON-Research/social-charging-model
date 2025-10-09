import pandas as pd
import matplotlib.pyplot as plt


# Set the Excel file name
excel_file = 'SCM_export_results_EVsperCP.xlsx'


# Read the first sheet (default)
df = pd.read_excel(excel_file)
# Read the second sheet for out of model charge, left without charging, left while charging
df2 = pd.read_excel(excel_file, sheet_name=1)

# Create two rows of three subplots: first row for success rate (sr), second for average probability (ar)
behaviors = ['b1', 'b2', 'b3']
unique_scenarios = df['scenario'].unique()


fig, axes = plt.subplots(2, 3, figsize=(7.2, 4.5))

# First row: success rate (sr)
for idx, behavior in enumerate(behaviors):
    ax = axes[0, idx]
    mean_col = f'm_sr_{behavior}'
    lower_col = f'l_sr_{behavior}'
    upper_col = f'u_sr_{behavior}'
    for scenario in unique_scenarios:
            data = df[df['scenario'] == scenario]
            data = data[data['timestep'] >= 96]
            ax.plot(data['timestep'], data[mean_col] * 100, label=f'Scenario {scenario} Mean', linewidth=1)
        #ax.fill_between(data['timestep'], data[lower_col], data[upper_col], alpha=0.2)
    ax.set_title(f'Success Rate: Behavior {behavior}', fontsize=8)
    # No x-axis label for first row
    if idx == 0:
        ax.set_ylabel('Success Rate (%)', fontsize=8)
    ax.tick_params(axis='both', labelsize=7)


# Second row: average probability (ar)
for idx, behavior in enumerate(behaviors):
    ax = axes[1, idx]
    mean_col = f'm_ap_{behavior}'
    lower_col = f'l_ap_{behavior}'
    upper_col = f'u_ap_{behavior}'
    for scenario in unique_scenarios:
            data = df[df['scenario'] == scenario]
            data = data[data['timestep'] >= 96]
            ax.plot(data['timestep'], data[mean_col] * 100, label=f'Scenario {scenario} Mean', linewidth=1)
        #ax.fill_between(data['timestep'], data[lower_col], data[upper_col], alpha=0.2)
    ax.set_title(f'Probability: Behavior {behavior}', fontsize=8)
    # No x-axis label for second row
    if idx == 0:
        ax.set_ylabel('Probability (%)', fontsize=8)
    ax.tick_params(axis='both', labelsize=7)


# --- Separate plot for out of model charge, left without charging, left while charging ---
metrics = [
    ('oomc', 'Out of Model Charge (kWh/day)'),
    ('lwc', 'Left Without Charging (#/day)'),
    ('luc', 'Left While Charging (#/day)')
]
unique_scenarios2 = df2['scenario'].unique()
fig2, axes2 = plt.subplots(1, 3, figsize=(7.2, 2.5))
for idx, (metric, title) in enumerate(metrics):
    ax = axes2[idx]
    mean_col = f'm_{metric}'
    lower_col = f'l_{metric}'
    upper_col = f'u_{metric}'
    for scenario in unique_scenarios2:
        data = df2[df2['scenario'] == scenario]
        data = data[data['day'] >= 3]
        rolling_mean = data[mean_col].rolling(window=60, min_periods=1).mean()
        ax.plot(data['day'], rolling_mean, label=f'Scenario {scenario} Rolling Mean')
        #ax.fill_between(data['day'], data[lower_col], data[upper_col], alpha=0.2)
    ax.set_title(title, fontsize=10)
    ax.set_xlabel('day', fontsize=10)
    ax.set_ylabel(title, fontsize=10)
    ax.tick_params(axis='both', labelsize=8)

# --- Cumulative mean plot ---
fig3, axes3 = plt.subplots(1, 3, figsize=(7.2, 2.5))
for idx, (metric, title) in enumerate(metrics):
    ax = axes3[idx]
    mean_col = f'm_{metric}'
    for scenario in unique_scenarios2:
        data = df2[df2['scenario'] == scenario]
        data = data[data['day'] >= 3]
        cummean = data[mean_col].expanding().mean()
        ax.plot(data['day'], cummean, label=f'Scenario {scenario} Cumulative Mean')
    ax.set_title(title + ' (Cumulative Mean)', fontsize=10)
    ax.set_xlabel('day', fontsize=10)
    ax.set_ylabel(title, fontsize=10)
    ax.tick_params(axis='both', labelsize=8)
handles3, labels3 = [], []
for ax in axes3.flat:
    h, l = ax.get_legend_handles_labels()
    for handle, label in zip(h, l):
        if label not in labels3:
            handles3.append(handle)
            labels3.append(label)
if handles3:
    fig3.legend(handles3, labels3, loc='lower center', ncol=min(len(labels3), 5), frameon=False, bbox_to_anchor=(0.5, 0.01))
fig3.suptitle('Charging metrics (Cumulative Mean)', fontsize=14)
fig3.tight_layout(rect=[0, 0.03, 1, 0.95])
fig3.savefig('plot_charging_metrics_EVsPerCP_cumulative_mean.pdf', bbox_inches='tight')
fig3.savefig('plot_charging_metrics_EVsPerCP_cumulative_mean.png', bbox_inches='tight', dpi=300)
plt.show()
handles2, labels2 = [], []
for ax in axes2.flat:
    h, l = ax.get_legend_handles_labels()
    for handle, label in zip(h, l):
        if label not in labels2:
            handles2.append(handle)
            labels2.append(label)
if handles2:
    fig2.legend(handles2, labels2, loc='lower center', ncol=min(len(labels2), 5), frameon=False, bbox_to_anchor=(0.5, 0.01))
fig2.suptitle('Charging metrics', fontsize=14)
fig2.tight_layout(rect=[0, 0.03, 1, 0.95])
fig2.savefig('plot_charging_metrics_EVsPerCP_scenario.pdf', bbox_inches='tight')
fig2.savefig('plot_charging_metrics_EVsPerCP_scenario.png', bbox_inches='tight', dpi=300)
plt.show()



# Add a single legend at the bottom spanning all columns using fig.legend
handles, labels = [], []
for ax in axes.flat:
    h, l = ax.get_legend_handles_labels()
    for handle, label in zip(h, l):
        if label not in labels:
            handles.append(handle)
            labels.append(label)
if handles:
    fig.legend(handles, labels, loc='upper center', ncol=min(len(labels), 5), frameon=False, bbox_to_anchor=(0.5, -0.12))
fig.suptitle('Average probability and success rate for different numbers of ECs per Charge Point', fontsize=14)
fig.tight_layout(rect=[0, 0.03, 1, 0.95])
fig.savefig('plot_prob_and_success_rate_EVsPerCP_scenario.pdf', bbox_inches='tight')
fig.savefig('plot_prob_and_success_rate_EVsPerCP_scenario.png', bbox_inches='tight', dpi=300)
plt.show()



