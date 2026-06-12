#Plot OOMC (out of model charge) per behaviour scenario

import pandas as pd
import matplotlib.pyplot as plt


# Define the behavior scenarios
subselection = [
    {'b1': True,  'b2': False, 'b3': False, 'b4': True,
     'label': 'B1', 'color': 'tab:green', 'linestyle': '-'},
    {'b1': False, 'b2': True,  'b3': False, 'b4': True,
     'label': 'B2', 'color': 'tab:red', 'linestyle': '-'},
    {'b1': False, 'b2': False, 'b3': True,  'b4': True,
     'label': 'B3', 'color': 'tab:orange', 'linestyle': '-'},
    {'b1': True,  'b2': True,  'b3': True,  'b4': True,
     'label': 'All behaviors', 'color': 'tab:purple', 'linestyle': '-'}
]

# Define which behaviors to exclude per subplot
exclude_map = {
    0: ['B2', 'B3', 'B2 and B3'],        # for plot 1 (B1)
    1: ['B1', 'B3', 'B1 and B3'],        # for plot 2 (B2)
    2: ['B1', 'B2', 'B1 and B2'],        # for plot 3 (B3)
}

# desired legend order
desired_order = [
    'B1',
    'B2',
    'B3',
    'All behaviors'
]

# One subplot per behaviour type
metrics = [
    ('Behaviour 1\n(moving)'),
    ('Behaviour 2\n(requesting)'),
    ('Behaviour 3\n(notifying)'),
]

# Set the Excel file name
excel_file = 'SCM_results_behaviours.xlsx'

df = pd.read_excel(excel_file, sheet_name=0)

width = 15.92 / 2.52  # width word cm to inch
height = width * (3 / 7)  # maintain aspect ratio
fig, axes = plt.subplots(1, 3, figsize=(width, height))

# dictionary to capture one handle per label (for the combined legend)
plot_handles = {}

for idx, title in enumerate(metrics):
    ax = axes[idx]
    mean_col  = 'm_oomcs'
    lower_col = 'l_oomcs'
    upper_col = 'u_oomcs'

    excluded_labels = exclude_map.get(idx, [])

    for sel in subselection:
        label = sel['label']
        if label in excluded_labels:
            continue

        mask = (
            (df['b1'] == sel['b1']) &
            (df['b2'] == sel['b2']) &
            (df['b3'] == sel['b3']) &
            (df['b4'] == sel['b4'])
        )

        data = df[mask & (df['week'] >= 42) & (df['EVsPerCP'] <= 15)].copy()
        if data.empty:
            continue

        data = data.sort_values(['charge_points', 'EVsPerCP', 'week'])

        # Keep only the smallest EVsPerCP per charge_points
        first_evs = (
            data.groupby('charge_points', as_index=False)['EVsPerCP']
            .min()
            .rename(columns={'EVsPerCP': 'first_EVsPerCP'})
        )
        data_filtered = data.merge(first_evs, on='charge_points')
        data_filtered = data_filtered[data_filtered['EVsPerCP'] == data_filtered['first_EVsPerCP']]
        data_filtered = data_filtered.drop(columns='first_EVsPerCP')

        # Aggregate mean and bounds across weeks for each charge_points
        data_mean = (
            data_filtered.groupby('charge_points', as_index=False)
            .agg({mean_col: 'mean', lower_col: 'mean', upper_col: 'mean', 'EVsPerCP': 'mean'})
            .sort_values('EVsPerCP')
        )

        line, = ax.plot(
            data_mean['EVsPerCP'],
            data_mean[mean_col],
            label=label,
            linestyle=sel['linestyle'],
            color=sel['color']
        )
        ax.fill_between(
            data_mean['EVsPerCP'],
            data_mean[lower_col],
            data_mean[upper_col],
            color=sel['color'],
            alpha=0.15
        )

        if label not in plot_handles:
            plot_handles[label] = line

    ax.set_title(title, fontsize=8, pad=10)
    ax.set_xlabel('EVs per CP', fontsize=8)
    ax.set_ylabel('OoMC (sessions)' if idx == 0 else None, fontsize=8)
    ax.tick_params(axis='both', labelsize=8)
    ax.set_xticks([5, 10, 15])

# Build the combined legend in the desired order
handles = [plot_handles[label] for label in desired_order if label in plot_handles]
labels  = [label for label in desired_order if label in plot_handles]

if handles:
    fig.legend(handles, labels,
               loc='lower center',
               ncol=min(len(labels), 4),
               frameon=False,
               bbox_to_anchor=(0.5, -0.05),
               fontsize=8)

fig.suptitle("Out of model charge (sessions)", fontsize=9)
fig.subplots_adjust(bottom=0.24, top=0.78, wspace=0.3)

fig.savefig('plot_oomc_EVsPerCP.png', bbox_inches='tight', dpi=300)

# ---------------------------------------------------------------------------
# Figure 2: OOMC evolution over weeks (one subplot per EVsPerCP value)
# ---------------------------------------------------------------------------

EV_values = [5, 10, 100/7]  # 100/7 ≈ 14.29

fig2, axes2 = plt.subplots(1, len(EV_values), figsize=(width, height), sharey=True)
week_handles = {}

for ax_i, (ax, ev_value) in enumerate(zip(axes2, EV_values)):
    for sel in subselection:
        mask = (
            (df['b1'] == sel['b1']) &
            (df['b2'] == sel['b2']) &
            (df['b3'] == sel['b3']) &
            (df['b4'] == sel['b4'])
        )

        data = df[mask & (df['EVsPerCP'] == ev_value) & (df['week'] >= 3)].copy()
        data = data.sort_values(['charge_points', 'week'])
        if data.empty:
            continue

        # Average across charge_points for each week, then smooth
        data_week = (
            data.groupby('week', as_index=False)
            .agg({'m_oomcs': 'mean', 'l_oomcs': 'mean', 'u_oomcs': 'mean'})
            .sort_values('week')
        )
        data_week['m_oomcs_smooth'] = data_week['m_oomcs'].rolling(window=10, min_periods=1).mean()
        data_week['l_oomcs_smooth'] = data_week['l_oomcs'].rolling(window=10, min_periods=1).mean()
        data_week['u_oomcs_smooth'] = data_week['u_oomcs'].rolling(window=10, min_periods=1).mean()

        label = sel['label'] if ax_i == 0 else None
        line, = ax.plot(
            data_week['week'],
            data_week['m_oomcs_smooth'],
            label=label,
            color=sel['color'],
            linestyle=sel['linestyle'],
            linewidth=1.5
        )
        ax.fill_between(
            data_week['week'],
            data_week['l_oomcs_smooth'],
            data_week['u_oomcs_smooth'],
            color=sel['color'],
            alpha=0.15
        )

        if sel['label'] not in week_handles:
            week_handles[sel['label']] = line

    title_value = round(ev_value, 1)
    ax.set_title(f'{title_value} EVs per CP', fontsize=8, pad=10)
    ax.set_xlabel('Week', fontsize=8)
    ax.tick_params(axis='both', labelsize=7, labelleft=True)

axes2[0].set_ylabel('OoMC (sessions)', fontsize=8)

week_legend_handles = [week_handles[l] for l in desired_order if l in week_handles]
week_legend_labels  = [l for l in desired_order if l in week_handles]

if week_legend_handles:
    fig2.legend(week_legend_handles, week_legend_labels,
                loc='lower center',
                ncol=min(len(week_legend_labels), 4),
                frameon=False,
                bbox_to_anchor=(0.5, -0.05),
                fontsize=8)

fig2.suptitle("Out of model charge per week (sessions)", fontsize=9)
fig2.subplots_adjust(bottom=0.24, top=0.78, wspace=0.15)

fig2.savefig('plot_oomc_perWeek.png', bbox_inches='tight', dpi=300)

plt.show()