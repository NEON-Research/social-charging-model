import pandas as pd
import matplotlib.pyplot as plt

# Simplified scenario selection and labels
subselection = [
    {'b1': False, 'b2': False, 'b3': False, 'b4': False,  'label': 'No behaviors'},
    {'b1': True,  'b2': False, 'b3': False, 'b4': False,  'label': 'Behavior 1'},
    {'b1': False, 'b2': True,  'b3': False, 'b4': False,  'label': 'Behavior 2'},
    {'b1': True,  'b2': False, 'b3': True,  'b4': False,  'label': 'Behavior 1 and 3'},
    {'b1': True,  'b2': True,  'b3': True,  'b4': False,  'label': 'All social behaviors'},
]

# Set the Excel file name
excel_file = 'SCM_results_behaviours.xlsx'

# Read the second sheet
df = pd.read_excel(excel_file, sheet_name=1)

abbr = 'kmd'
title = 'Kilometers Driven\n(per Week)'

# ✅ Create the figure and three subplots
fig, axes = plt.subplots(1, 3, figsize=(10, 3))
ax_weekly, ax_running, ax_cum = axes

# --- Plot data ---
for sel in subselection:
    mask = (
        (df['b1'] == sel['b1']) &
        (df['b2'] == sel['b2']) &
        (df['b3'] == sel['b3']) &
        (df['b4'] == sel['b4'])
    )
    
    data = df[mask & (df['EVsPerCP'] == 10)].copy()
    if data.empty:
        continue

    mean_col = f'm_{abbr}'
    data_filtered_weeks = data[data['week'] >= 0].copy()

    # Compute running and cumulative mean
    running_mean = data_filtered_weeks[mean_col].rolling(window=3, min_periods=1).mean()
    cum_mean = data_filtered_weeks[mean_col].expanding().mean()

    label = sel['label']

    # Plot all three
    ax_weekly.plot(data_filtered_weeks['week'], data_filtered_weeks[mean_col], label=label)
    ax_running.plot(data_filtered_weeks['week'], running_mean, label=label)
    ax_cum.plot(data_filtered_weeks['week'], cum_mean, label=label)

# --- Titles ---
ax_weekly.set_title(title + " (Weekly)", fontsize=9, pad=8)
ax_running.set_title(title + " (Running Mean)", fontsize=9, pad=8)
ax_cum.set_title(title + " (Cumulative Mean)", fontsize=9, pad=8)

# --- Axis labels ---
for ax in axes:
    ax.set_xlabel('Week', fontsize=9)
    ax.set_ylabel(None)
    ax.tick_params(axis='both', labelsize=8)

# --- Combine legend across subplots ---
handles, labels = [], []
for ax in axes:
    h, l = ax.get_legend_handles_labels()
    for handle, label in zip(h, l):
        if label not in labels:
            handles.append(handle)
            labels.append(label)

if handles:
    legend = fig.legend(
        handles,
        labels,
        loc='lower center',
        ncol=min(len(labels), 5),
        frameon=False,
        fontsize=8,
        bbox_to_anchor=(0.5, -0.02) 
    )

# --- Adjust layout to make space for legend ---
fig.subplots_adjust(bottom=0.3)
plt.show()