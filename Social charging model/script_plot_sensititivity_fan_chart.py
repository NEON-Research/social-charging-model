import pandas as pd
import matplotlib.pyplot as plt


excel_file = 'SCM_export_results_sensitivity.xlsx'
df = pd.read_excel(excel_file, sheet_name=1)
print("Columns:", df.columns)

metrics = ['oomc', 'lwc', 'lwcda', 'lu', 'pscs', 'cs', 'rcs']
fig, axes = plt.subplots(1, len(metrics), figsize=(2.5*len(metrics), 2.5))

for idx, metric in enumerate(metrics):
	ax = axes[idx]
	grouped = df.groupby('day')[metric]
	mean = grouped.mean().rolling(window=14, min_periods=1).mean()
	p5 = grouped.quantile(0.05).rolling(window=14, min_periods=1).mean()
	p95 = grouped.quantile(0.95).rolling(window=14, min_periods=1).mean()
	p10 = grouped.quantile(0.10).rolling(window=14, min_periods=1).mean()
	p90 = grouped.quantile(0.90).rolling(window=14, min_periods=1).mean()
	p25 = grouped.quantile(0.25).rolling(window=14, min_periods=1).mean()
	p75 = grouped.quantile(0.75).rolling(window=14, min_periods=1).mean()
	ax.plot(mean.index, mean.values, label='Mean')
	ax.fill_between(mean.index, p5.values, p95.values, alpha=0.2, color='blue', label='5-95%')
	ax.fill_between(mean.index, p10.values, p90.values, alpha=0.2, color='green', label='10-90%')
	ax.fill_between(mean.index, p25.values, p75.values, alpha=0.2, color='orange', label='25-75%')
	ax.set_title(metric, fontsize=10)
	ax.set_xlabel('Day', fontsize=10)
	ax.set_ylabel(metric, fontsize=10)
	ax.tick_params(axis='both', labelsize=8)

fig.legend(['Mean', '5-95%', '10-90%', '25-75%'], loc='lower center', ncol=4, frameon=False, bbox_to_anchor=(0.5, 0.01))
fig.suptitle('Fan Charts for Simulation Metrics', fontsize=14)
fig.tight_layout(rect=[0, 0.03, 1, 0.95])
fig.savefig('fan_charts_metrics.pdf', bbox_inches='tight')
fig.savefig('fan_charts_metrics.png', bbox_inches='tight', dpi=300)
plt.show()
