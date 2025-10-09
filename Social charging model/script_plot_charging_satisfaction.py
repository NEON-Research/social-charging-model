import pandas as pd
import matplotlib.pyplot as plt

excel_file = 'SCM_export_results.xlsx'
df2 = pd.read_excel(excel_file, sheet_name=1)

metrics = [
    ('cs', 'Charging Satisfaction'),
    ('cspd', 'Charging Sessions/Day'),
    ('rcspd', 'Missed Charging Sessions/Day')
]

unique_scenarios2 = df2['scenario'].unique()
fig, axes = plt.subplots(1, 3, figsize=(7.2, 2.5))

for idx, (abbr, title) in enumerate(metrics):
    ax = axes[idx]
    mean_col = f'm_{abbr}'
    lower_col = f'l_{abbr}'
    upper_col = f'u_{abbr}'
    for scenario in unique_scenarios2:
        data = df2[(df2['scenario'] == scenario) & (df2['day'] >= 13)]
        ax.plot(data['day'], data[mean_col], label=f'Scenario {scenario} Mean')
        #ax.fill_between(data['day'], data[lower_col], data[upper_col], alpha=0.2)
    ax.set_title(title, fontsize=10)
    ax.set_xlabel('Day', fontsize=10)
    ax.set_ylabel(title, fontsize=10)
    ax.tick_params(axis='both', labelsize=8)

handles, labels = [], []
for ax in axes.flat:
    h, l = ax.get_legend_handles_labels()
    for handle, label in zip(h, l):
        if label not in labels:
            handles.append(handle)
            labels.append(label)
if handles:
    fig.legend(handles, labels, loc='lower center', ncol=min(len(labels), 5), frameon=False, bbox_to_anchor=(0.5, 0.01))

fig.suptitle('Charging Satisfaction, Sessions/Day, Missed Sessions/Day', fontsize=14)
fig.tight_layout(rect=[0, 0.03, 1, 0.95])
fig.savefig('plot_charging_metrics.pdf', bbox_inches='tight')
fig.savefig('plot_charging_metrics.png', bbox_inches='tight', dpi=300)
plt.show()