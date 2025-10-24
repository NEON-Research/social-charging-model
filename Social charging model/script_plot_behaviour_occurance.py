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
    ('sib1', 'Successful B1 (# per week)'),
    ('sib2', 'Successful B2 (# per week)'),
    ('sib3', 'Successful B3 (# per week)'),
]

metrics2 = [
    ('usib1', 'unsuccessful B1 (# per week)'),
    ('usib2', 'unuccessful B2 (# per week)'),
    ('usib3', 'unSuccessful B3 (# per week)'),
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
                
        # filter your data based on mask (but keep all weeks)
        data = df[mask].copy()

        # sort by EVsPerCP so the smallest EVsPerCP for each charge_points is kept
        data = data.sort_values('EVsPerCP')

        # drop duplicates based on charge_points, keeping first (smallest EVsPerCP)
        data_unique_cp = data.drop_duplicates(subset='charge_points', keep='first')

        # compute the mean of 'mean_col' across all weeks for each charge_points
        data_mean = (
            data_unique_cp.groupby('charge_points', as_index=False)
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
        line, = ax.plot(data_mean['EVsPerCP'], data_mean[mean_col], label=label, linestyle='-')

        # Plot corresponding unsuccessful behavior
        unsuccess_abbr, _ = metrics2[idx]
        unsuccess_mean_col = f'm_{unsuccess_abbr}'

        # compute mean for unsuccessful column
        data_mean_unsuccess = (
            data_unique_cp.groupby('charge_points', as_index=False)
            .agg({unsuccess_mean_col: 'mean', 'EVsPerCP': 'mean'})
        )
        data_mean_unsuccess = data_mean_unsuccess.sort_values('EVsPerCP')
        
        ax.plot(
            data_unique_cp['EVsPerCP'],
            data_unique_cp[unsuccess_mean_col],
            linestyle='--',
            color=line.get_color(),
            label=f"{label} (Unsuccessful)"
        )

        # Inspect what data is being averaged
        print("Unique combinations before mean:")
        print(data[['charge_points', 'EVsPerCP', 'week', mean_col]].sort_values('charge_points').head(20))

        # Check grouping behavior
        check_group = data[data['charge_points'] == some_charge_point_value]
        print(check_group[['charge_points', 'EVsPerCP', 'week', mean_col]])

            # --- Styling logic ---
        # if label == "No behaviors":
        #     # Plot normally and store its color
        #     line, = ax.plot(data_filtered_weeks['week'], data_filtered_weeks[mean_col], label=label, linestyle='-')
        #     base_color = line.get_color()

        # elif label == "No behaviors, daily availability check":
        #     # Use same color but dashed line
        #     ax.plot(data_filtered_weeks['week'], data_filtered_weeks[mean_col], label=label, linestyle='--', color=base_color)

        # else:
        #     # Plot normally for other behaviors
        #     ax.plot(data_filtered_weeks['week'], data_filtered_weeks[mean_col], label=label) 

        # Optional: add fill_between for uncertainty
        # ax.fill_between(data['day'], data[lower_col], data[upper_col], alpha=0.2)

    ax.set_title(title, fontsize=8, pad=10)
    ax.set_xlabel('EVs per CP', fontsize=8)
    ax.set_ylabel(None)
    ax.tick_params(axis='both', labelsize=6)
    ax.set_xticks([5, 10, 15, 20]) 

 # --- Control decimal places on y-axis ---
    # if idx == 0:
    #     ax.set_yticks([60, 70, 80, 90])
    # elif idx == 1:
    #     ax.set_yticks([7, 8, 9, 10]) 
    # elif idx == 2:
    #     ax.set_yticks([10, 11, 12, 13])  

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
fig.savefig('plot_behaviour_occurance_EVsPerCP.pdf', bbox_inches='tight')
fig.savefig('plot_behaviour_occurance_EVsPerCP.png', bbox_inches='tight', dpi=300)

plt.show()