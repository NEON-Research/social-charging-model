import pandas as pd
import matplotlib.pyplot as plt

# Simplified scenario selection and labels
subselection = [
    {'b1': False, 'b2': False, 'b3': False, 'b4': False,  'label': 'No behaviors'},
    # {'b1': False,  'b2': False, 'b3': False, 'b4': True,  'label': 'No behaviors, daily availability check'},
    {'b1': True,  'b2': False, 'b3': False, 'b4': False,  'label': 'Behavior 1'},
    {'b1': False, 'b2': True,  'b3': False, 'b4': False,  'label': 'Behavior 2'},
    {'b1': True,  'b2': False, 'b3': True,  'b4': False,  'label': 'Behavior 1 and 3'},
    {'b1': True,  'b2': True,  'b3': True,  'b4': False,  'label': 'All social behaviors'},
]


# Set the Excel file name
excel_file = 'SCM_results_behaviours.xlsx'

# Read the second sheet for out of model charge, left without charging, left while charging
df = pd.read_excel(excel_file, sheet_name=1)

metrics = [
    ('probb1', 'Behavior 1'),
    ('probb2', 'Behavior 2'),
    ('probb3', 'Behavior 3')
]


fig, axes = plt.subplots(1, 3, figsize=(7.2, 3))
for idx, (abbr, title) in enumerate(metrics):
    ax = axes[idx]
    mean_col = f'm_{abbr}'
    lower_col = f'l_{abbr}'
    upper_col = f'u_{abbr}'
    for sel in subselection:
        mask = (
            (df['b1'] == sel['b1']) &
            (df['b2'] == sel['b2']) &
            (df['b3'] == sel['b3']) &
            (df['b4'] == sel['b4'])
        )
                
        data = df[mask & 
            (df['EVsPerCP'] == 10)
       ].copy()
        
         # --- Divide by 7 for 'cspd' and 'rcspd' ---
        if abbr in ['cspd', 'rcspd']:
            data[mean_col] /= 7
            data[lower_col] /= 7
            data[upper_col] /= 7

        # --- * 100 for % in cs
        if abbr in ['cs']:
            data[mean_col] *= 100
            data[lower_col] *= 100
            data[upper_col] *= 100 

        label = sel['label']
        if data.empty:
            #print(f"No data found for scenario {label} at EVsPerCP = {target_ev}")
            continue

        evs_per_cp = data['EVsPerCP'].iloc[0] if 'EVsPerCP' in data.columns else 'NA'
       
        # Plot normally for other behaviors
        ax.plot(data['week'], data[mean_col], label=label) 


    ax.set_title(title, fontsize=8, pad=10)
    ax.set_xlabel('Week', fontsize=8)
    ax.set_ylabel(None)
    ax.tick_params(axis='both', labelsize=6)

 # --- Control decimal places on y-axis ---
    # if idx == 0:
    #     ax.set_yticks([50, 60, 70, 80, 90, 100])
    # elif idx == 1:
    #     ax.set_yticks([7, 8, 9, 10]) 
    # elif idx == 2:
    #     ax.set_yticks([11, 12, 13, 14, 15])  

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

# --- Save with tight bounding box ---
#fig.savefig('plot_charging_satisfaction_perWeek_5EVsPerCP.pdf', bbox_inches='tight')
fig.savefig('plot_prob_behaviours_perWeek_10EVsPerCP.png', bbox_inches='tight', dpi=300)
#fig.savefig('plot_charging_satisfaction_perWeek_10EVsPerCP.png', bbox_inches='tight', dpi=300)

plt.show()
