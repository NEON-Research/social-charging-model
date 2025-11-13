import pandas as pd
import matplotlib.pyplot as plt

# Define the behavior scenarios
subselection1 = [
    {'b1': False, 'b2': False, 'b3': False, 'b4': False,
     'label': 'No behaviors', 'color': 'tab:blue', 'linestyle': '-'},
    {'b1': True,  'b2': False, 'b3': False, 'b4': False,
     'label': 'Behavior 1', 'color': 'tab:green', 'linestyle': '-'},
    {'b1': False, 'b2': True,  'b3': False, 'b4': False,
     'label': 'Behavior 2', 'color': 'tab:red', 'linestyle': '-'},
    {'b1': False, 'b2': False, 'b3': True,  'b4': False,
     'label': 'Behavior 3', 'color': 'tab:orange', 'linestyle': '-'},
    {'b1': True,  'b2': True,  'b3': False, 'b4': False,
     'label': 'Behavior 1 and 2', 'color': 'tab:cyan', 'linestyle': '--'},
    {'b1': True,  'b2': False, 'b3': True,  'b4': False,
     'label': 'Behavior 1 and 3', 'color': 'tab:olive', 'linestyle': '--'},
    {'b1': False, 'b2': True,  'b3': True,  'b4': False,
     'label': 'Behavior 2 and 3', 'color': 'tab:brown', 'linestyle': '--'},
    {'b1': True,  'b2': True,  'b3': True,  'b4': False,
     'label': 'All social behaviors', 'color': 'tab:purple', 'linestyle': '-'}
]

# Load data
excel_file = 'SCM_results_behaviours.xlsx'
df = pd.read_excel(excel_file, sheet_name=1)

# --- Setup plot ---
fig, ax = plt.subplots(figsize=(3.3, 3.85))

# --- Plot charging satisfaction for each scenario ---
for sel in subselection1:
    mask = (
        (df['b1'] == sel['b1']) &
        (df['b2'] == sel['b2']) &
        (df['b3'] == sel['b3']) &
        (df['b4'] == sel['b4'])
    )

    data = df[mask & (df['week'] >= 42) & (df['EVsPerCP'] <= 15)].copy()
    data = data.sort_values(['charge_points', 'EVsPerCP', 'week'])

    # Convert to percentage
    data['m_cs'] *= 100

    # Aggregate mean by charge_points
    data_mean = (
        data.groupby('charge_points', as_index=False)
        .agg({'m_cs': 'mean', 'EVsPerCP': 'mean'})
    ).sort_values('EVsPerCP')

    if data_mean.empty:
        continue

    ax.plot(
        data_mean['EVsPerCP'],
        data_mean['m_cs'],
        label=sel['label'],
        color=sel['color'],
        linestyle=sel['linestyle'],
        linewidth=2
    )

# --- Format plot ---
ax.set_title('Charging fulfillment ratio\n(% of required charging sessions fulfilled)', fontsize=9, pad=15)
ax.set_xlabel('EVs per CP', fontsize=8)
#ax.set_ylabel('Charging Satisfaction (%)', fontsize=9)
ax.tick_params(axis='both', labelsize=8)
ax.set_xlim(1, 15)
ax.set_xticks([5, 10, 15])


# --- Legend and layout ---
fig.legend(loc='lower center',
           ncol=min(len(subselection1), 4),
           frameon=False,
           bbox_to_anchor=(0.5, -0.05),
           fontsize=8)

fig.subplots_adjust(bottom=0.2)

# --- Save plot ---
fig.savefig('plot_charging_satisfaction_EVsPerCP.png', bbox_inches='tight', dpi=300)

summary_rows = []  # list to store results for export

print("\n--- Scenario summary ---")

for sel in subselection1:
    mask = (
        (df['b1'] == sel['b1']) &
        (df['b2'] == sel['b2']) &
        (df['b3'] == sel['b3']) &
        (df['b4'] == sel['b4'])
    )

    data = df[mask & (df['week'] >= 42) & (df['EVsPerCP'] <= 15)].copy()
    data = data.sort_values(['charge_points', 'EVsPerCP', 'week'])

    # Convert to percentage
    data['m_cs'] *= 100

    # Aggregate mean by charge_points (averaging over all weeks ≥42)
    data_mean = (
        data.groupby('charge_points', as_index=False)
        .agg({'m_cs': 'mean', 'EVsPerCP': 'mean'})
        .sort_values('EVsPerCP')
    )

    if data_mean.empty:
        print(f"{sel['label']}: no data available")
        summary_rows.append({
            'Scenario': sel['label'],
            'Drop below 90% at EVsPerCP': None,
            'm_cs at drop (%)': None,
            'm_cs at 14 EVsPerCP (%)': None
        })
        continue

    # --- Find where m_cs drops below 90% ---
    below_90 = data_mean[data_mean['m_cs'] < 90]
    if not below_90.empty:
        first_row = below_90.iloc[0]
        first_ev = first_row['EVsPerCP']
        first_mcs = first_row['m_cs']
        drop_text = f"m_cs drops below 90% at EVsPerCP = {first_ev:.1f} (m_cs = {first_mcs:.2f}%)"
    else:
        first_ev = None
        first_mcs = None
        drop_text = "m_cs stays above 90% for all EVsPerCP ≤ 15"

    # --- Find the value at EVsPerCP = 14 ---
    val_14 = data_mean.loc[data_mean['EVsPerCP'].round() == 14, 'm_cs']
    if not val_14.empty:
        mcs_14 = val_14.iloc[0]
        val_text = f"m_cs at 14 EVsPerCP = {mcs_14:.2f}%"
    else:
        mcs_14 = None
        val_text = f"no data for EVsPerCP = 14"

    # --- Print summary ---
    print(f"{sel['label']}: {drop_text}; {val_text}")

    # --- Store summary for Excel ---
    summary_rows.append({
        'Scenario': sel['label'],
        'Drop below 90% at EVsPerCP': first_ev,
        'm_cs at drop (%)': first_mcs,
        'm_cs at 14 EVsPerCP (%)': mcs_14
    })

# --- Create DataFrame and export to Excel ---
summary_df = pd.DataFrame(summary_rows)
summary_df.to_excel('charging_satisfaction_summary.xlsx', index=False)

print("\n✅ Summary table saved as 'm_cs_summary.xlsx'")

#     # --- Find the value at EVsPerCP = 14 ---
#     val_14 = data_mean.loc[data_mean['EVsPerCP'].round() == 14, 'm_cs']
#     if not val_14.empty:
#         mcs_14 = val_14.iloc[0]
#         val_text = f"m_cs at 14 EVsPerCP = {mcs_14:.2f}%"
#     else:
#         val_text = f"no data for EVsPerCP = 14"

#     print(f"{sel['label']}: {drop_text}; {val_text}")

# # Compute mean of m_luc for No behaviors and Behavior 1 with 14 EVsPerCP and week >= 42

# for sel in subselection1:
#     if sel['label'] in ['No behaviors', 'Behavior 1']:
#         mask = (
#             (df['b1'] == sel['b1']) &
#             (df['b2'] == sel['b2']) &
#             (df['b3'] == sel['b3']) &
#             (df['b4'] == sel['b4']) &
#             (df['EVsPerCP'] == 14) &
#             (df['week'] >= 42)
#         )

#         data = df[mask].copy()

#         if not data.empty:
#             mean_val = data['m_luc'].mean()
#             print(f"{sel['label']}: mean m_luc = {mean_val:.3f}")
#         else:
#             print(f"{sel['label']}: no data found for week >= 42 and 14 EVsPerCP")

# #--------------------------------------------------------------------

# # Define the behavior scenarios
# subselection2 = [
#     {'b1': False, 'b2': False, 'b3': False, 'b4': True,
#      'label': 'No behaviors', 'color': 'tab:blue', 'linestyle': '-'},
#     {'b1': True,  'b2': False, 'b3': False, 'b4': True,
#      'label': 'Behavior 1', 'color': 'tab:green', 'linestyle': '-'},
#     {'b1': False, 'b2': True,  'b3': False, 'b4': True,
#      'label': 'Behavior 2', 'color': 'tab:red', 'linestyle': '-'},
#     {'b1': False, 'b2': False, 'b3': True,  'b4': True,
#      'label': 'Behavior 3', 'color': 'tab:orange', 'linestyle': '-'},
#     {'b1': True,  'b2': True,  'b3': False, 'b4': True,
#      'label': 'Behavior 1 and 2', 'color': 'tab:cyan', 'linestyle': '--'},
#     {'b1': True,  'b2': False, 'b3': True,  'b4': True,
#      'label': 'Behavior 1 and 3', 'color': 'tab:olive', 'linestyle': '--'},
#     {'b1': False, 'b2': True,  'b3': True,  'b4': True,
#      'label': 'Behavior 2 and 3', 'color': 'tab:brown', 'linestyle': '--'},
#     {'b1': True,  'b2': True,  'b3': True,  'b4': True,
#      'label': 'All social behaviors', 'color': 'tab:purple', 'linestyle': '-'}
# ]


# # --- Setup plot ---
# fig2, ax = plt.subplots(figsize=(3.3, 3.85))

# # --- Plot charging satisfaction for each scenario ---
# for sel in subselection2:
#     mask = (
#         (df['b1'] == sel['b1']) &
#         (df['b2'] == sel['b2']) &
#         (df['b3'] == sel['b3']) &
#         (df['b4'] == sel['b4'])
#     )

#     data = df[mask & (df['week'] >= 42) & (df['EVsPerCP'] <= 15)].copy()
#     data = data.sort_values(['charge_points', 'EVsPerCP', 'week'])

#     # Convert to percentage
#     data['m_cs'] *= 100

#     # Aggregate mean by charge_points
#     data_mean = (
#         data.groupby('charge_points', as_index=False)
#         .agg({'m_cs': 'mean', 'EVsPerCP': 'mean'})
#     ).sort_values('EVsPerCP')

#     if data_mean.empty:
#         continue

#     ax.plot(
#         data_mean['EVsPerCP'],
#         data_mean['m_cs'],
#         label=sel['label'],
#         color=sel['color'],
#         linestyle=sel['linestyle'],
#         linewidth=2
#     )

# # --- Format plot ---
# ax.set_title('Charging fulfillment ratio in scenarios with recheck\n(% of required charging sessions fulfilled)', fontsize=9, pad=15)
# ax.set_xlabel('EVs per CP', fontsize=8)
# #ax.set_ylabel('Charging Satisfaction (%)', fontsize=9)
# ax.tick_params(axis='both', labelsize=8)
# ax.set_xlim(1, 15)
# ax.set_xticks([5, 10, 15])


# # --- Legend and layout ---
# fig2.legend(loc='lower center',
#            ncol=min(len(subselection1), 4),
#            frameon=False,
#            bbox_to_anchor=(0.5, -0.05),
#            fontsize=8)

# fig2.subplots_adjust(bottom=0.2)

# # --- Save plot ---
# fig2.savefig('plot_charging_satisfaction_EVsPerCP_wRecheck.png', bbox_inches='tight', dpi=300)

plt.show()
