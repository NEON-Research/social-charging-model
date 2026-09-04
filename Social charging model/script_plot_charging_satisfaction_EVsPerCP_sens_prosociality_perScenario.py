"""
Charging fulfillment ratio vs EVs per CP, one panel per behaviour scenario,
one line per prosociality multiplier (lambda).

Scenario -> hue        (matching script_plot_charging_satisfaction_EVsPerCP.py)
Lambda   -> lightness within that hue, plus linestyle as redundant encoding
            (light/dotted = 0.5, base/solid = 1.0, dark/dashed = 1.5)

Bands are the 5th-95th percentile across Monte Carlo runs, as produced by
f_getUncertaintyBounds (columns l_cs / u_cs).

Reads  SCM_results_behaviours_sensProsociality.xlsx
Writes plot_charging_satisfaction_EVsPerCP_sens_prosociality_perScenario.png
"""

import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.colors as mcolors
from matplotlib.lines import Line2D
from matplotlib.patches import Patch

EXCEL_FILE = 'SCM_results_behaviours_sensProsociality.xlsx'
OUT_PNG = 'plot_charging_satisfaction_EVsPerCP_sens_prosociality_perScenario.png'

# 'all' shades every lambda arm, 'baseline' only lambda = 1.0, 'none' lines only
BAND_MODE = 'all'
BAND_ALPHA = 0.13

# True: lambda varies lightness within the scenario hue. False: one flat hue per
# panel, lambda carried by linestyle alone (the earlier version).
SHADE_BY_LAMBDA = True

# Optional grey reference from the main behaviours run. That scenario is
# lambda-invariant by construction, but comes from a different file with a
# different iteration count -- state the cross-run comparison if you enable it.
SHOW_NO_BEHAVIOUR_REFERENCE = False
NO_BEHAVIOUR_FILE = 'SCM_results_behaviours.xlsx'

scenarios = [
    {'b1': True,  'b2': False, 'b3': False, 'b4': True,
     'title': 'B1 (moving)',     'color': 'tab:green'},
    {'b1': False, 'b2': True,  'b3': False, 'b4': True,
     'title': 'B2 (requesting)', 'color': 'tab:red'},
    {'b1': False, 'b2': False, 'b3': True,  'b4': True,
     'title': 'B3 (notifying)',  'color': 'tab:orange'},
    {'b1': True,  'b2': True,  'b3': True,  'b4': True,
     'title': 'All behaviours',  'color': 'tab:purple'},
]

# 'mix' < 0 lightens toward white, > 0 darkens toward black
lambdas = [
    {'value': 0.50, 'linestyle': ':',  'mix': -0.52, 'label': r'$\lambda$ = 0.50  (half of the measured probability)'},
    {'value': 1.00, 'linestyle': '-',  'mix':  0.00, 'label': r'$\lambda$ = 1.00  (measured probability)'},
    {'value': 1.50, 'linestyle': '--', 'mix':  0.34, 'label': r'$\lambda$ = 1.50  (1.5 $\times$ measured probability)'},
]


def shade(color, mix):
    """Blend a colour toward white (mix<0) or black (mix>0)."""
    r, g, b = mcolors.to_rgb(color)
    if mix < 0:
        f = -mix
        return (r + (1 - r) * f, g + (1 - g) * f, b + (1 - b) * f)
    f = mix
    return (r * (1 - f), g * (1 - f), b * (1 - f))


def aggregate(data):
    """Mean over weeks >= 42, collapsed to one row per charge point level."""
    out = (
        data.groupby('charge_points', as_index=False)
        .agg({'m_cs': 'mean', 'l_cs': 'mean', 'u_cs': 'mean', 'EVsPerCP': 'mean'})
        .sort_values('EVsPerCP')
    )
    for c in ('m_cs', 'l_cs', 'u_cs'):
        out[c] *= 100
    return out


df = pd.read_excel(EXCEL_FILE, sheet_name=0)

ref = None
if SHOW_NO_BEHAVIOUR_REFERENCE:
    rdf = pd.read_excel(NO_BEHAVIOUR_FILE, sheet_name=0)
    rmask = (~rdf['b1']) & (~rdf['b2']) & (~rdf['b3']) & (rdf['b4'])
    ref = aggregate(rdf[rmask & (rdf['week'] >= 42)].copy())

fig, axes = plt.subplots(1, len(scenarios), figsize=(9.2, 3.1), sharey=True)

for ax, sc in zip(axes, scenarios):
    if ref is not None:
        ax.plot(ref['EVsPerCP'], ref['m_cs'], color='0.55',
                linestyle='-', linewidth=1.2, zorder=1)

    series = []
    for lam in lambdas:
        mask = (
            (df['b1'] == sc['b1']) &
            (df['b2'] == sc['b2']) &
            (df['b3'] == sc['b3']) &
            (df['b4'] == sc['b4']) &
            (df['prosocialityFactor'] == lam['value'])
        )
        data = df[mask & (df['week'] >= 42)].copy()
        if data.empty:
            continue
        col = shade(sc['color'], lam['mix']) if SHADE_BY_LAMBDA else sc['color']
        series.append((lam, aggregate(data), col))

    # bands first, so no line is buried under a later fill
    for lam, agg, col in series:
        if BAND_MODE == 'all' or (BAND_MODE == 'baseline' and lam['value'] == 1.0):
            ax.fill_between(agg['EVsPerCP'], agg['l_cs'], agg['u_cs'],
                            color=col, alpha=BAND_ALPHA, linewidth=0, zorder=2)
    for lam, agg, col in series:
        ax.plot(agg['EVsPerCP'], agg['m_cs'], color=col,
                linestyle=lam['linestyle'], linewidth=2, zorder=3)

    ax.set_title(sc['title'], fontsize=9)
    ax.set_xlabel('EVs per CP', fontsize=8)
    ax.tick_params(axis='both', labelsize=8)
    ax.set_xlim(1, 17)
    ax.set_xticks([5, 10, 15])
    ax.set_ylim(0, 102)
    ax.set_yticks([0, 20, 40, 60, 80, 100])
    ax.grid(axis='y', linestyle=':', linewidth=0.6, color='0.85', zorder=0)
    ax.set_axisbelow(True)

axes[0].set_ylabel('Charging fulfillment ratio (%)', fontsize=8)

# Legend swatches use a neutral hue, since the hue itself is the panel's identity
handles = []
for lam in lambdas:
    grey = shade('tab:gray', lam['mix']) if SHADE_BY_LAMBDA else '0.25'
    handles.append(Line2D([0], [0], color=grey, linewidth=2,
                          linestyle=lam['linestyle'], label=lam['label']))
handles.append(Patch(facecolor='0.55', alpha=0.30, edgecolor='none',
                     label='5th-95th percentile of runs'))
if ref is not None:
    handles.append(Line2D([0], [0], color='0.55', linewidth=1.2,
                          label='No behaviours (reference run)'))

fig.legend(handles=handles, loc='lower center', ncol=len(handles),
           frameon=False, bbox_to_anchor=(0.5, -0.09), fontsize=8)

fig.tight_layout()
fig.savefig(OUT_PNG, bbox_inches='tight', dpi=300)

print("\n--- Fulfillment (%) at weeks >= 42, by scenario and lambda ---")
rows = []
for sc in scenarios:
    m = ((df['b1'] == sc['b1']) & (df['b2'] == sc['b2']) &
         (df['b3'] == sc['b3']) & (df['b4'] == sc['b4']) & (df['week'] >= 42))
    for lam in lambdas:
        d = df[m & (df['prosocialityFactor'] == lam['value'])]
        if d.empty:
            continue
        rows.append({'scenario': sc['title'], 'lambda': lam['value'],
                     'mean_all_CP': d['m_cs'].mean() * 100,
                     'at_16.7_EVsPerCP': d[d['charge_points'] == 6]['m_cs'].mean() * 100,
                     'at_10_EVsPerCP': d[d['charge_points'] == 10]['m_cs'].mean() * 100})
print(pd.DataFrame(rows).round(2).to_string(index=False))

plt.show()
