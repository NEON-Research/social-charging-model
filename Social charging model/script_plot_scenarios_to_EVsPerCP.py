import pandas as pd
import matplotlib.pyplot as plt
import textwrap

# Simplified scenario selection and labels
subselection = [
    {'b1': False, 'b2': False, 'b3': False, 'b4': False,  'label': 'No behaviors'},
    # {'b1': False,  'b2': False, 'b3': False, 'b4': True,  'label': 'No behaviors, daily availability check'},
    {'b1': True,  'b2': False, 'b3': False, 'b4': False,  'label': 'Behavior 1'},
    {'b1': False, 'b2': True,  'b3': False, 'b4': False,  'label': 'Behavior 2'},
    {'b1': False, 'b2': False,  'b3': True, 'b4': False,  'label': 'Behavior 3'},
    {'b1': True,  'b2': True, 'b3': False,  'b4': False,  'label': 'Behavior 1 and 2'},
    {'b1': True,  'b2': False, 'b3': True,  'b4': False,  'label': 'Behavior 1 and 3'},
    {'b1': False,  'b2': True, 'b3': True,  'b4': False,  'label': 'Behavior 2 and 3'},
    {'b1': True,  'b2': True,  'b3': True,  'b4': False,  'label': 'All social behaviors'},
]

# # Simplified scenario selection and labels
# subselection = [
#     {'b1': False, 'b2': False, 'b3': False, 'b4': True,  'label': 'No behaviors'},
#     # {'b1': False,  'b2': False, 'b3': False, 'b4': True,  'label': 'No behaviors, daily availability check'},
#     {'b1': True,  'b2': False, 'b3': False, 'b4': True,  'label': 'Behavior 1'},
#     {'b1': False, 'b2': True,  'b3': False, 'b4': True,  'label': 'Behavior 2'},
#     {'b1': False,  'b2': False, 'b3': True,  'b4': True,  'label': 'Behavior 3'},
#     {'b1': True,  'b2': True,  'b3': True,  'b4': True,  'label': 'All social behaviors'},
# ]


# Set the Excel file name
excel_file = 'SCM_results_behaviours.xlsx'


# Read the first sheet (default)
df = pd.read_excel(excel_file, sheet_name=1)

metrics = [
    ('cs', 'Charging Satisfaction\n(% of satisfied charging sessions)'),
    ('cspd', 'Charging Sessions\n(daily avg)'),
    ('rcspd', 'Required charging sessions\n(daily avg)')
]

unique_scenarios = df['scenario'].unique()
fig, axes = plt.subplots(1, 3, figsize=(7.5, 3))


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
        data = df[mask & (df['week'] >= 42) & (df['EVsPerCP'] <= 15)].copy()

        # sort by EVsPerCP so the smallest EVsPerCP for each charge_points is kept
        data = data.sort_values(['charge_points', 'EVsPerCP', 'week'])

        # Find the smallest EVsPerCP per charge_points
        # first_evs = (
        #     data.groupby('charge_points', as_index=False)['EVsPerCP']
        #     .min()
        #     .rename(columns={'EVsPerCP': 'first_EVsPerCP'})
        # )

        # Merge to keep only matching rows
        # data_filtered = data.merge(first_evs, on='charge_points')
        # data_filtered = data_filtered[data_filtered['EVsPerCP'] == data_filtered['first_EVsPerCP']]

        # Drop helper column
        # data_filtered = data_filtered.drop(columns='first_EVsPerCP')
        
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


        # compute the mean of 'mean_col' across all weeks for each charge_points
        data_mean = (
            data.groupby('charge_points', as_index=False)
            .agg({mean_col: 'mean', 'EVsPerCP': 'mean'})
        )  
        
        # 5. Sort the result for plotting
        data_mean = data_mean.sort_values('EVsPerCP')

          

        if data_mean.empty:
            continue


        label = sel['label']

        # # --- Styling logic ---
        # if label == "No behaviors":
        #     # plot normally and capture its color
        #     line, = ax.plot(data_mean['EVsPerCP'], data_mean[mean_col],
        #                     label=label, linestyle='-')
        #     base_color = line.get_color()

        # elif label == "No behaviors, daily availability check":
        #     # use same color but dashed line
        #     ax.plot(data_mean['EVsPerCP'], data_mean[mean_col],
        #             label=label, linestyle='--', color=base_color)

        # else:
        #     # default style
        #     ax.plot(data_mean['EVsPerCP'], data_mean[mean_col],
        #             label=label)
        ax.plot(data_mean['EVsPerCP'], data_mean[mean_col], label=label)    

    #ax.set_title(title, fontsize=10)
      # --- Wrap long titles into 2 lines ---
    #wrapped_title = "\n".join(textwrap.wrap(title, width=35))
    ax.set_title(title, fontsize=9, pad=8)
    ax.set_xlabel('EVs per CP', fontsize=9)
    # ax.set_ylabel(title, fontsize=10)
    ax.set_ylabel(None)
    ax.tick_params(axis='both', labelsize=8)
    ax.set_xlim(1, 15)                           # Force axis range 1–20
    ax.set_xticks([5, 10, 15])            

# --- Place legend below all subplots ---
handles, labels = [], []
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
               ncol=min(len(labels), 5),
               frameon=False,
               bbox_to_anchor=(0.5, -0.05),  # put it below the plots
               fontsize=8)

# Add a bit of margin below for the legend
fig.subplots_adjust(bottom=0.2)

# # --- Save with tight bounding box ---
# fig.savefig('plot_charging_satisfaction_EVsPerCP_wRecheck.pdf', bbox_inches='tight')
# fig.savefig('plot_charging_satisfaction_EVsPerCP_wRecheck.png', bbox_inches='tight', dpi=300)

#--- Save with tight bounding box ---
fig.savefig('plot_charging_satisfaction_EVsPerCP.pdf', bbox_inches='tight')
fig.savefig('plot_charging_satisfaction_EVsPerCP.png', bbox_inches='tight', dpi=300)

plt.show()

