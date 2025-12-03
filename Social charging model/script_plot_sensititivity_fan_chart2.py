import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle
from matplotlib.lines import Line2D
from matplotlib.patches import Patch
import pandas as pd

# -------- Custom Stacked Legend Patch --------
class StackedBands(Rectangle):
    def __init__(self, width=1.0, height=1.0, colors=None, alphas=None):
        super().__init__((0,0), width, height, facecolor="none")
        self.colors = colors
        self.alphas = alphas

    def draw(self, renderer):
        for color, alpha in zip(self.colors, self.alphas):
            r = Rectangle(
                self.get_xy(), self.get_width(), self.get_height(),
                facecolor=color, edgecolor="none", alpha=alpha
            )
            r.set_transform(self.get_transform())
            r.draw(renderer)

# -----------------------------
# Updated band colors & alpha
# -----------------------------
band_colors = ["#9ecae1", "#6baed6", "#3182bd"]  # 90%, 80%, 50%
band_labels = ["90%", "80%", "50%"]
band_alpha = 0.6

#stacked_handle = StackedBands(colors=band_colors, alphas=band_alphas)

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
subselection1 = [
    {'b1': False, 'b2': False, 'b3': False, 'b4': False,
     'label': 'No behaviors', 'color': 'tab:blue', 'linestyle': '-'},

    {'b1': True,  'b2': True,  'b3': True,  'b4': False,
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


# ============================================================
# FIGURE 2 — Trips & Km driven with same band colors
# ============================================================

fig2, axes2 = plt.subplots(1, 2, figsize=(15.92 / 2.52, (15.92 / 2.52)*(3/7)))
pairs = [("trips", "No behaviors"), ("kmd", "No behaviors")]
title_map = {"trips": "Trips\n(avg per car per week)", "kmd": "Kilometers driven\n(avg per car per week)"}

for ax, (metric, scenario) in zip(axes2, pairs):
    sel = next(s for s in subselection1 if s['label']==scenario)
    mask = (df['b1']==sel['b1']) & (df['b2']==sel['b2']) & (df['b3']==sel['b3']) & (df['b4']==sel['b4'])
    data = df[mask].copy()
    g = compute_group(data, metric)

    # Mean line
    ax.plot(g['EVsPerCP'], g[f'{metric}_m'], color=sel['color'], linestyle=sel['linestyle'], linewidth=2)

    # Percentile bands
    for l,u,color in zip([f'{metric}_l90', f'{metric}_l80', f'{metric}_l50'],
                         [f'{metric}_u90', f'{metric}_u80', f'{metric}_u50'],
                         band_colors):
        ax.fill_between(g['EVsPerCP'], g[l], g[u], color=color, alpha=band_alpha)

    ax.set_title(title_map[metric], fontsize=8)
    ax.set_xlabel("EVs per CP", fontsize=8)
    ax.set_ylim(bottom=0)
    ax.set_xticks([5,10,15])
    ax.tick_params(axis='both', labelsize=8)
    ax.grid(True)

# Legend: mean + percentile bands
mean_line = Line2D([],[],color=sel['color'], linestyle=sel['linestyle'], linewidth=2, label="Mean")
band_handles = [Patch(facecolor=c, alpha=band_alpha, label=l) for c,l in zip(band_colors, band_labels)]
fig2.legend(handles=[mean_line]+band_handles, loc='lower center', ncol=4, frameon=False, fontsize=8, bbox_to_anchor=(0.5,-0.12))

fig2.subplots_adjust(bottom=0.25)
fig2.savefig('plot_confidence_interval_trips_kmd_with_bands.png', dpi=300, bbox_inches='tight')
plt.show()
