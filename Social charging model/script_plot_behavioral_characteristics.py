import pandas as pd
import matplotlib.pyplot as plt

# Set the Excel file name
excel_file = 'SCM_results_behaviours.xlsx'

# Read the second sheet for out of model charge, left without charging, left while charging
df = pd.read_excel(excel_file, sheet_name=1)

subselection = [{'b1': True,  'b2': True,  'b3': True,  'b4': True, 'EVsPerCP': 5,  'label': 'All behaviors 5 EVs per CP'},
                {'b1': True,  'b2': True,  'b3': True,  'b4': True, 'EVsPerCP': 10,  'label': 'All behaviors 10 EVs per CP'},
                {'b1': True,  'b2': True,  'b3': True,  'b4': True, 'EVsPerCP': 19,  'label': 'All behaviors 20 EVs per CP'}
                ]
# subselection = [{'b1': False,  'b2': True,  'b3': True,  'b4': True, 'EVsPerCP': 5,  'label': 'All behaviors 5 EVs per CP'},
#                 {'b1': False,  'b2': True,  'b3': True,  'b4': True, 'EVsPerCP': 10,  'label': 'All behaviors 10 EVs per CP'},
#                 {'b1': False,  'b2': True,  'b3': True,  'b4': True, 'EVsPerCP': 19,  'label': 'All behaviors 20 EVs per CP'}
#                 ]

metrics = [
    ('pcp', 'Perceived CP pressure'),
    #('rc', 'Reputational concern'), 
    ('psi', 'Perceived social interdependence'),
    ('n1', 'Norm behavior 1'),   # Norm behavior 1
    ('n2', 'Norm behavior 2'),   # Norm behavior 2 
    ('n3', 'Norm behavior 3')    # Norm behavior 3
]



fig, axes = plt.subplots(1, 3, figsize=(7.2, 3))
for idx, sel in enumerate(subselection):
    ax = axes[idx]
    
    for met in metrics:
        mask = (
            (df['b1'] == sel['b1']) &
            (df['b2'] == sel['b2']) &
            (df['b3'] == sel['b3']) &
            (df['b4'] == sel['b4']) &
            (df['EVsPerCP'] == sel['EVsPerCP'])
        )
                
        data = df[mask].copy()
        
        label = sel['label']
        if data.empty:
            print(f"No data found for scenario {label} at EVsPerCP = {sel['EVsPerCP']}")
            continue

        mean_col = f'm_{met[0]}'
    
        # Plot normally for other behaviors
        ax.plot(data['week'], data[mean_col], label=met[1]) 


    ax.set_title(sel['label'], fontsize=8, pad=10)
    ax.set_xlabel('Week', fontsize=8)
    ax.set_ylabel(None)
    ax.set_ylim(0,1)
    ax.tick_params(axis='both', labelsize=6)


# --- Combine legend entries across subplots ---
handles, labels = [], []
for ax in axes.flat:
    h, l = ax.get_legend_handles_labels()
    for handle, label in zip(h, l):
        if label not in labels:
            handles.append(handle)
            labels.append(label)

if handles:
    fig.legend(
        handles, labels,
        loc='lower center',
        ncol=min(len(labels), 5),
        frameon=False,
        bbox_to_anchor=(0.5, -0.05),
        fontsize=6
    )

# Add a bit of margin below for the legend
fig.subplots_adjust(bottom=0.18, top=0.8,  wspace=0.35)  # optional: add top margin too

plt.tight_layout()

# --- Save with tight bounding box ---
#fig.savefig('plot_charging_satisfaction_perWeek_5EVsPerCP.pdf', bbox_inches='tight')
fig.savefig('plot_metrics_perWeek.png', bbox_inches='tight', dpi=300)
#fig.savefig('plot_charging_satisfaction_perWeek_10EVsPerCP.png', bbox_inches='tight', dpi=300)

plt.show()
