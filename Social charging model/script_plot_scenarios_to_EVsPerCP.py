import pandas as pd
import matplotlib.pyplot as plt
import textwrap

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
        subset = df[mask & (df['week'] == 51)].copy()

        # --- Divide by 7 for 'cspd' and 'rcspd' ---
        if abbr in ['cspd', 'rcspd']:
            subset[mean_col] /= 7
            subset[lower_col] /= 7
            subset[upper_col] /= 7

        # --- * 100 for % in cs
        if abbr in ['cs']:
            subset[mean_col] *= 100
            subset[lower_col] *= 100
            subset[upper_col] *= 100    

        if subset.empty:
            continue

        # sort by EVsPerCP so the smallest EVsPerCP for each charge_points is kept
        subset = subset.sort_values('EVsPerCP')

        # drop duplicates based on charge_points, keeping first (smallest EVsPerCP)
        subset_unique_cp = subset.drop_duplicates(subset='charge_points', keep='first')

        # now group by EVsPerCP and compute mean (if you still have multiple runs per EVsPerCP)
        # grouped = subset_unique_cp.groupby('EVsPerCP')[mean_col].mean().reset_index()


        label = sel['label']

        # --- Styling logic ---
        if label == "No behaviors":
            # plot normally and capture its color
            line, = ax.plot(subset_unique_cp['EVsPerCP'], subset_unique_cp[mean_col],
                            label=label, linestyle='-')
            base_color = line.get_color()

        elif label == "No behaviors, daily availability check":
            # use same color but dashed line
            ax.plot(subset_unique_cp['EVsPerCP'], subset_unique_cp[mean_col],
                    label=label, linestyle='--', color=base_color)

        else:
            # default style
            ax.plot(subset_unique_cp['EVsPerCP'], subset_unique_cp[mean_col],
                    label=label)
            

    #ax.set_title(title, fontsize=10)
      # --- Wrap long titles into 2 lines ---
    #wrapped_title = "\n".join(textwrap.wrap(title, width=35))
    ax.set_title(title, fontsize=9, pad=8)
    ax.set_xlabel('EVs per CP', fontsize=9)
    # ax.set_ylabel(title, fontsize=10)
    ax.set_ylabel(None)
    ax.tick_params(axis='both', labelsize=8)
    ax.set_xlim(1, 20)                           # Force axis range 1–20
    ax.set_xticks([5, 10, 15, 20])            

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

# --- Save with tight bounding box ---
fig.savefig('plot_charging_satisfaction_EVsPerCP.pdf', bbox_inches='tight')
fig.savefig('plot_charging_satisfaction_EVsPerCP.png', bbox_inches='tight', dpi=300)

plt.show()

