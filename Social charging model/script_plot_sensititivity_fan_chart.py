import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle
from matplotlib.lines import Line2D
from matplotlib.patches import Patch
import pandas as pd


# -------------------------
# Load data
# -------------------------
excel_file = 'SCM_results_behaviours.xlsx'
df = pd.read_excel(excel_file, sheet_name=0)

# Only last 10 weeks
df = df[(df['week'] >= 42) & (df['week'] <= 51)]

# -------------------------
# Behavior scenarios
# -------------------------
# subselection1 = [
#     {'b1': False, 'b2': False, 'b3': False, 'b4': False,
#      'label': 'No behaviors', 'color': 'tab:blue', 'linestyle': '-'},

#     {'b1': True,  'b2': True,  'b3': True,  'b4': False,
#      'label': 'All social behaviors', 'color': 'tab:purple', 'linestyle': '-'}
# ]
subselection1 = [
    {'b1': False, 'b2': False, 'b3': False, 'b4': True,
     'label': 'No behaviors', 'color': 'tab:blue', 'linestyle': '-'},

    {'b1': True,  'b2': True,  'b3': True,  'b4': True,
     'label': 'All social behaviors', 'color': 'tab:purple', 'linestyle': '-'}
]

# -------------------------
# Helper: extract averaged metric bands
# -------------------------
def compute_group(data, metric):

    data = data[(data["week"] >= 42) & (data["week"] <= 51)].copy()

    grouped = (
        data.groupby("EVsPerCP", as_index=False)
        .agg({
            f"{metric}_m": "mean",
            f"{metric}_l90": "mean",
            f"{metric}_u90": "mean",
            f"{metric}_l80": "mean",
            f"{metric}_u80": "mean",
            f"{metric}_l50": "mean",
            f"{metric}_u50": "mean",
        })
        .sort_values("EVsPerCP")
    )

    # ---------------------------------------------------
    # Scaling rules
    # ---------------------------------------------------
    if metric == "cfr":
        scale = 100.0        # convert to %
    else:
        scale = 1.0 / 100.0  # per car (100 cars)

    for col in [
        f"{metric}_m",
        f"{metric}_l90", f"{metric}_u90",
        f"{metric}_l80", f"{metric}_u80",
        f"{metric}_l50", f"{metric}_u50",
    ]:
        grouped[col] *= scale

    return grouped


# ============================================================
# FIGURE 1 — three-panel plot
# ============================================================

metrics_fig1 = {
    "cfr": "Charging fulfillment ratio (%)",
    "cs": "Charging sessions\n(avg per car per week)",
    "rcs": "Required charging sessions\n(avg per car per week)"
}

width = 15.92 / 2.52
height = width * (3 / 7)
fig1, axes1 = plt.subplots(1, 3, figsize=(width, height))

for ax, (metric, title) in zip(axes1, metrics_fig1.items()):

    for sel in subselection1:

        mask = (
            (df['b1'] == sel['b1']) &
            (df['b2'] == sel['b2']) &
            (df['b3'] == sel['b3']) &
            (df['b4'] == sel['b4'])
        )

        data = df[mask]
        if data.empty:
            continue

        g = compute_group(data, metric)

        # Mean line
        ax.plot(
            g['EVsPerCP'], g[f'{metric}_m'],
            label=sel['label'], color=sel['color'],
            linestyle=sel['linestyle'], linewidth=2
        )

        # Percentile bands
        ax.fill_between(g['EVsPerCP'], g[f'{metric}_l90'], g[f'{metric}_u90'], alpha=0.1, color=sel['color'])
        ax.fill_between(g['EVsPerCP'], g[f'{metric}_l80'], g[f'{metric}_u80'], alpha=0.1, color=sel['color'])
        ax.fill_between(g['EVsPerCP'], g[f'{metric}_l50'], g[f'{metric}_u50'],  alpha=0.1, color=sel['color'])

    ax.set_title(title, fontsize=8)
    ax.set_xlabel("EVs per CP", fontsize=8)
    ax.set_ylim(bottom=0)
    ax.set_xticks([5, 10, 15])
    ax.tick_params(axis='both', labelsize=8)
    ax.grid(True)

# Custom legend for scenarios
legend_handles = [
    Line2D([0], [0], color=s['color'], linestyle=s['linestyle'],
           linewidth=2, label=s['label'])
    for s in subselection1
]

fig1.legend(
    handles=legend_handles,
    loc='lower center',
    ncol=4,
    frameon=False,
    fontsize=8
)

fig1.tight_layout(rect=[0, 0.08, 1, 1])
fig1.savefig('plot_confidence_charging_satisfaction.png', dpi=300, bbox_inches='tight')



#test
# ============================================================
# FIGURE 2 — Simplified Trips & Km driven (same style as Fig1)
# ============================================================

# fig2, axes2 = plt.subplots(1, 2, figsize=(15.92 / 2.52, (15.92 / 2.52)*(3/7)))
# pairs = [("trips", "No behaviors"), ("kmd", "No behaviors")]
# title_map = {"trips": "Trips\n(avg per car per week)", "kmd": "Kilometers driven\n(avg per car per week)"}

# # Alpha levels matching Fig1
# band_alphas = [0.1, 0.1, 0.1]  # for 90%, 80%, 50% bands

# for ax, (metric, scenario) in zip(axes2, pairs):
#     sel = next(s for s in subselection1 if s['label']==scenario)
#     mask = (df['b1']==sel['b1']) & (df['b2']==sel['b2']) & (df['b3']==sel['b3']) & (df['b4']==sel['b4'])
#     data = df[mask].copy()
#     g = compute_group(data, metric)

#     # Mean line
#     ax.plot(g['EVsPerCP'], g[f'{metric}_m'], color=sel['color'], linestyle=sel['linestyle'], linewidth=2)

#     # Percentile bands — same color as scenario, different alphas
#     for l, u, alpha in zip([f'{metric}_l90', f'{metric}_l80', f'{metric}_l50'],
#                             [f'{metric}_u90', f'{metric}_u80', f'{metric}_u50'],
#                             band_alphas):
#         ax.fill_between(g['EVsPerCP'], g[l], g[u], color=sel['color'], alpha=alpha)

#     ax.set_title(title_map[metric], fontsize=8)
#     ax.set_xlabel("EVs per CP", fontsize=8)
#     ax.set_ylim(bottom=0)
#     ax.set_xticks([5, 10, 15])
#     ax.tick_params(axis='both', labelsize=8)
#     ax.grid(True)

# fig2.tight_layout()
# fig2.savefig('plot_confidence_interval_trips_kmd_simple.png', dpi=300, bbox_inches='tight')
# plt.show()


from matplotlib.lines import Line2D
from matplotlib.patches import Patch

# ============================================================
# FIGURE 2 — Simplified with legend
# ============================================================

fig2, axes2 = plt.subplots(1, 2, figsize=(15.92 / 2.52, (15.92 / 2.52)*(3/7)))
pairs = [("trips", "No behaviors"), ("kmd", "No behaviors")]
title_map = {"trips": "Trips\n(avg per car per week)", "kmd": "Kilometres driven\n(avg per car per week)"}

band_alphas = [0.1, 0.1, 0.1]  # same as Fig1

for ax, (metric, scenario) in zip(axes2, pairs):
    sel = next(s for s in subselection1 if s['label']==scenario)
    mask = (df['b1']==sel['b1']) & (df['b2']==sel['b2']) & (df['b3']==sel['b3']) & (df['b4']==sel['b4'])
    data = df[mask].copy()
    g = compute_group(data, metric)

    # Mean line
    ax.plot(g['EVsPerCP'], g[f'{metric}_m'], color=sel['color'], linestyle=sel['linestyle'], linewidth=2)

    # Percentile bands
    for l, u, alpha in zip([f'{metric}_l90', f'{metric}_l80', f'{metric}_l50'],
                            [f'{metric}_u90', f'{metric}_u80', f'{metric}_u50'],
                            band_alphas):
        ax.fill_between(g['EVsPerCP'], g[l], g[u], color=sel['color'], alpha=alpha)

    ax.set_title(title_map[metric], fontsize=8)
    ax.set_xlabel("EVs per CP", fontsize=8)
    ax.set_ylim(bottom=0)
    ax.set_xticks([5, 10, 15])
    ax.tick_params(axis='both', labelsize=8)
    ax.grid(True)

# -------------------
# Create legend
# -------------------
mean_line = Line2D([], [], color='tab:blue', linestyle='-', linewidth=2, label='Mean')  # color doesn't matter much
band_patch = Patch(facecolor='tab:blue', alpha=0.1, label='50%, 80%, and 90% uncertainty intervals')      # use one of the band colors

fig2.legend(handles=[mean_line, band_patch], loc='lower center', ncol=2, frameon=False, fontsize=8, bbox_to_anchor=(0.5, -0.12))

fig2.tight_layout()
fig2.savefig('plot_confidence_interval_trips_kmd_with_legend.png', dpi=300, bbox_inches='tight')
plt.show()


