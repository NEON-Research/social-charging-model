import pandas as pd
import matplotlib.pyplot as plt

# Simplified scenario selection and labels
# subselection = [
#     # {'b1': False, 'b2': False, 'b3': False, 'b4': False,  'label': 'No behaviors'},
#     # {'b1': False,  'b2': False, 'b3': False, 'b4': True,  'label': 'No behaviors, daily availability check'},
#     {'b1': True,  'b2': False, 'b3': False, 'b4': False,  'label': 'B1'},
#     {'b1': False, 'b2': True,  'b3': False, 'b4': False,  'label': 'B2'},
#     {'b1': False,  'b2': False, 'b3': True,  'b4': False,  'label': 'B3'},
#     # {'b1': True,  'b2': False, 'b3': True,  'b4': False,  'label': 'B1 and B3'},
#     {'b1': True,  'b2': True,  'b3': True,  'b4': False,  'label': 'All behaviors'},
# ]

# Define the behavior scenarios
subselection = [
    {'b1': True,  'b2': False, 'b3': False, 'b4': False,
     'label': 'B1', 'color': 'tab:green', 'linestyle': '-'},
    {'b1': False, 'b2': True,  'b3': False, 'b4': False,
     'label': 'B2', 'color': 'tab:red', 'linestyle': '-'},
    {'b1': False, 'b2': False, 'b3': True,  'b4': False,
     'label': 'B3', 'color': 'tab:orange', 'linestyle': '-'},
    # {'b1': True,  'b2': True,  'b3': False, 'b4': False,
    #  'label': 'B1 and B2', 'color': 'tab:cyan', 'linestyle': '--'},
    # {'b1': True,  'b2': False, 'b3': True,  'b4': False,
    #  'label': 'B1 and B3', 'color': 'tab:olive', 'linestyle': '--'},
    # {'b1': False, 'b2': True,  'b3': True,  'b4': False,
    #  'label': 'B2 and B3', 'color': 'tab:brown', 'linestyle': '--'},
    {'b1': True,  'b2': True,  'b3': True,  'b4': False,
     'label': 'All behaviors', 'color': 'tab:purple', 'linestyle': '-'}
]


# Define which behaviors to exclude per subplot
exclude_map = {
    0: ['B2', 'B3', 'B2 and B3'],        # for plot 1 (sib1)
    1: ['B1', 'B3', 'B1 and B3'],        # for plot 2 (sib2)
    2: ['B1', 'B2', 'B1 and B2'],        # for plot 3 (sib3)
}

# desired legend order
desired_order = [
    #'No behaviors',
    'B1',
    'B2',
    'B3',
    # 'B1 and B2',
    # 'B1 and B3',
    # 'B2 and B3',
    'All behaviors'
]

width = 15.92 / 2.52 # width word cm to inch
height = width * (3 / 7)  # maintain aspect ratio
fig, axes = plt.subplots(1, 3, figsize=(width, height))

# dictionary to capture one handle per label (for the combined legend)
plot_handles = {}


# Set the Excel file name
excel_file = 'SCM_results_behaviours.xlsx'

# Read the second sheet for out of model charge, left without charging, left while charging
df = pd.read_excel(excel_file, sheet_name=1)

metrics = [
    ('sib1', 'Behavior 1'),
    ('sib2', 'Behavior 2'),
    ('sib3', 'Behavior 3'),
]

metrics2 = [
    ('usib1', 'B1 unsuccessful'),
    ('usib2', 'B2 unuccessful'),
    ('usib3', 'B3 unsuccessful'),
]

# --- Create proportional sib/usib columns (normalized by m_cspd) ---
for b in ['b1', 'b2', 'b3']:
    sib_col = f'm_si{b}'
    usib_col = f'm_usi{b}'
    psib_col = f'm_psi{b}'
    pusib_col = f'm_pusi{b}'

    # Only create columns if they exist to avoid KeyErrors
    if sib_col in df.columns:
        df[psib_col] = df[sib_col] / df['m_cspd'] * 100
    if usib_col in df.columns:
        df[pusib_col] = df[usib_col] / df['m_cspd'] * 100

fig, axes = plt.subplots(1, 3, figsize=(7.2, 3))
for idx, (abbr, title) in enumerate(metrics):
    ax = axes[idx]
    mean_col = f'm_p{abbr}'
    #lower_col = f'l_{abbr}'
    #upper_col = f'u_{abbr}'

    # Get excluded labels for this plot
    excluded_labels = exclude_map.get(idx, [])

    for sel in subselection:
        label = sel['label']
        if label in excluded_labels:
            continue  # skip this behavior for this plot
    
        mask = (
            (df['b1'] == sel['b1']) &
            (df['b2'] == sel['b2']) &
            (df['b3'] == sel['b3']) &
            (df['b4'] == sel['b4'])
        )

                
        # filter your data based on mask (but keep all weeks)
        data = df[mask & (df['week'] >= 42)].copy()

        # sort by EVsPerCP so the smallest EVsPerCP for each charge_points is kept
        data = data.sort_values(['charge_points', 'EVsPerCP', 'week'])

        # Find the smallest EVsPerCP per charge_points
        first_evs = (
            data.groupby('charge_points', as_index=False)['EVsPerCP']
            .min()
            .rename(columns={'EVsPerCP': 'first_EVsPerCP'})
        )

        # Merge to keep only matching rows
        data_filtered = data.merge(first_evs, on='charge_points')
        data_filtered = data_filtered[data_filtered['EVsPerCP'] == data_filtered['first_EVsPerCP']]

        # Drop helper column
        data_filtered = data_filtered.drop(columns='first_EVsPerCP')

        # compute the mean of 'mean_col' across all weeks for each charge_points
        data_mean = (
            data_filtered.groupby('charge_points', as_index=False)
            .agg({mean_col: 'mean', 'EVsPerCP': 'mean'})
        )  
        
        # 5. Sort the result for plotting
        data_mean = data_mean.sort_values('EVsPerCP')

        label = sel['label']
        if data.empty:
            #print(f"No data found for scenario {label} at EVsPerCP = {target_ev}")
            continue

        #evs_per_cp = data['EVsPerCP'].iloc[0] if 'EVsPerCP' in data.columns else 'NA'
        
               # --- Styling logic ---
        # Plot main behavior
        success_label = f"{label} successful"
        line, = ax.plot(
            data_mean['EVsPerCP'],
            data_mean[mean_col],
            label=success_label,
            linestyle='-',
            color=sel['color']
        )

        # store one handle per unique label
        if success_label not in plot_handles:
            plot_handles[success_label] = line

        # Plot corresponding unsuccessful behavior
        unsuccess_abbr, _ = metrics2[idx]
        unsuccess_mean_col = f'm_p{unsuccess_abbr}'

        # compute mean for unsuccessful column
        data_mean_unsuccess = (
            data_filtered.groupby('charge_points', as_index=False)
            .agg({unsuccess_mean_col: 'mean', 'EVsPerCP': 'mean'})
        )
        data_mean_unsuccess = data_mean_unsuccess.sort_values('EVsPerCP')
        
        unsuccess_label = f"{label} unsuccessful"
        line_unsuccess, = ax.plot(
            data_mean_unsuccess['EVsPerCP'],
            data_mean_unsuccess[unsuccess_mean_col],
            linestyle='--',
            color=sel['color'],
            label=unsuccess_label
        )

        if unsuccess_label not in plot_handles:
            plot_handles[unsuccess_label] = line_unsuccess


    ax.set_title(title, fontsize=8, pad=10)
    ax.set_xlabel('EVs per CP', fontsize=8)
    ax.set_ylabel(None)
    ax.tick_params(axis='both', labelsize=8)
    ax.set_xticks([5, 10, 15]) 
    ax.set_yticks([0, 10, 20, 30, 40, 50, 60, 70])#, 15, 20, 25, 30])

 # --- Control decimal places on y-axis ---
    # if idx == 0:
    #     ax.set_yticks([60, 70, 80, 90])
    # elif idx == 1:
    #     ax.set_yticks([7, 8, 9, 10]) 
    # elif idx == 2:
    #     ax.set_yticks([10, 11, 12, 13])  

# Build the combined legend in the desired order
legend_order = []
for label in desired_order:
    legend_order.append(f"{label} successful")
    legend_order.append(f"{label} unsuccessful")
legend_order = [
    "B1 successful",
    "B1 unsuccessful",
    "B2 successful",
    "B2 unsuccessful",
    "B3 successful",
    "B3 unsuccessful",
    "All behaviors successful",
    "All behaviors unsuccessful",  # <-- now directly below
]

handles = [plot_handles[label] for label in legend_order if label in plot_handles]
labels = [label for label in legend_order if label in plot_handles]

#handles, labels = [], []
for ax in axes.flat:
    h, l = ax.get_legend_handles_labels()
    for handle, label in zip(h, l):
        if label not in labels:
            handles.append(handle)
            labels.append(label)

if handles:
    # add legend in a new figure row at the bottom (works with constrained_layout)
    fig.legend(handles, labels,
               loc='lower center',
               ncol=min(len(labels), 4),
               frameon=False,
               bbox_to_anchor=(0.5, -0.05),  # put it below the plots
               fontsize=8)

fig.suptitle("Occurance behaviors (% of charging sessions)", fontsize=9)
fig.subplots_adjust(bottom=0.24, top=0.8, wspace=0.35)

# --- Save with tight bounding box ---
fig.savefig('plot_behaviour_occurance_EVsPerCP.pdf', bbox_inches='tight')
fig.savefig('plot_behaviour_occurance_EVsPerCP.png', bbox_inches='tight', dpi=300)

plt.show()