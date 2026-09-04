"""
Diffusion of prosocial behaviour under different initial prosociality levels.

Answers the reviewer's question directly: if the initial level of prosociality is
lower than that observed among current EV owners, does the norm-learning mechanism
still generate uptake over the simulated year, or does the result collapse?

Reads  SCM_results_behaviours_sensProsociality.xlsx
Writes plot_prosociality_diffusion.png
"""

import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.lines import Line2D

EXCEL_FILE = 'SCM_results_behaviours_sensProsociality.xlsx'

# Charge-point level at which to show the trajectories.
# Set to None to average over all charge-point levels.
CHARGE_POINTS = 10

# The scenario the paper's headline result rests on: all three behaviours active.
SCENARIO = {'b1': True, 'b2': True, 'b3': True, 'b4': True}

prosociality_styles = [
    {'value': 0.50, 'color': 'tab:red',    'label': r'$\lambda$ = 0.50'},
    {'value': 1.00, 'color': 'tab:blue',   'label': r'$\lambda$ = 1.00 (baseline)'},
    {'value': 1.50, 'color': 'tab:green',  'label': r'$\lambda$ = 1.50'},
]

# B3 is omitted: prob_b3 == prob_b2 by construction (f_addEVOwner).
# The B2 panel has no uncertainty band on purpose -- the 'u_probb2' column in the export
# is written from getAvgProb_b1() (Startup_agent.f_writeBehaviorScenariosToExcel:1387),
# so it is the B1 upper bound, not B2's. Fix that line in the model and add
# 'lower': 'l_probb2', 'upper': 'u_probb2' back here.
panels = [
    {'mean': 'm_probb1', 'lower': 'l_probb1', 'upper': 'u_probb1',
     'title': 'B1 — move vehicle',            'ylabel': 'mean p(B1)'},
    {'mean': 'm_probb2',
     'title': 'B2 — request move',            'ylabel': 'mean p(B2)'},
    {'mean': 'm_cs',     'lower': 'l_cs',     'upper': 'u_cs',
     'title': 'Charging fulfillment ratio',   'ylabel': 'fulfillment (%)', 'scale': 100},
]

df = pd.read_excel(EXCEL_FILE, sheet_name=0)

base_mask = (
    (df['b1'] == SCENARIO['b1']) &
    (df['b2'] == SCENARIO['b2']) &
    (df['b3'] == SCENARIO['b3']) &
    (df['b4'] == SCENARIO['b4'])
)
if CHARGE_POINTS is not None:
    base_mask &= (df['charge_points'] == CHARGE_POINTS)

fig, axes = plt.subplots(1, len(panels), figsize=(9.5, 3.4), sharex=True)

for ax, panel in zip(axes, panels):
    scale = panel.get('scale', 1)

    for ps in prosociality_styles:
        data = df[base_mask & (df['prosocialityFactor'] == ps['value'])].copy()
        if data.empty:
            continue

        agg = {panel['mean']: 'mean'}
        has_band = 'lower' in panel and 'upper' in panel
        if has_band:
            agg[panel['lower']] = 'mean'
            agg[panel['upper']] = 'mean'

        series = (
            data.groupby('week', as_index=False)
            .agg(agg)
            .sort_values('week')
        )

        ax.plot(series['week'], series[panel['mean']] * scale,
                color=ps['color'], linewidth=1.8)
        if has_band:
            ax.fill_between(series['week'],
                            series[panel['lower']] * scale,
                            series[panel['upper']] * scale,
                            color=ps['color'], alpha=0.12, linewidth=0)

    ax.set_title(panel['title'], fontsize=9)
    ax.set_xlabel('Week', fontsize=8)
    ax.set_ylabel(panel['ylabel'], fontsize=8)
    ax.tick_params(axis='both', labelsize=8)
    ax.spines[['top', 'right']].set_visible(False)

handles = [Line2D([0], [0], color=ps['color'], linewidth=2, label=ps['label'])
           for ps in prosociality_styles]

subtitle = 'all behaviours active'
if CHARGE_POINTS is not None:
    subtitle += f', {CHARGE_POINTS} charge points'
fig.suptitle(f'Diffusion of prosocial charging behaviour ({subtitle})', fontsize=10)

fig.legend(handles=handles, loc='lower center', ncol=4, frameon=False,
           bbox_to_anchor=(0.5, -0.06), fontsize=8)

fig.tight_layout(rect=[0, 0.02, 1, 0.94])
fig.savefig('plot_prosociality_diffusion.png', bbox_inches='tight', dpi=300)

plt.show()
