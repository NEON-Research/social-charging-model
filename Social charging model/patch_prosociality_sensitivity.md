# Prosociality sensitivity test — implementation patch

**Model:** Social charging model (AnyLogic 8.9.7, split `_alp` format)
**Purpose:** robustness check requested by Reviewer 1 — "explore the effects of lower initial levels of prosociality or weaker behavioural uptake"
**Status:** patch specification. Nothing in the model has been changed yet.

---

## 1. How prosociality is currently set

There is no single "prosocial norm" parameter. The initial level of prosociality is drawn
empirically from the survey and enters the model through three linked places.

### 1.1 The empirical source

`social_psychological_variables.xlsx` — read at startup by `Startup_agent`:

| Function | File / line | What it reads |
|---|---|---|
| `f_getSortedSocialPsychologicalData()` | `_alp/Agents/Startup_agent/Code/Functions.java:1139` | sheet `frequency_list` → `sortedRealData` (empirical CDFs, **n = 288**) in the order `[norms, rc, psi, pcp, b1, b2, b3]` |
| `f_getCorrelationMatrixFromExcel()` | `…:1122` | sheet `correlation_matrix` → 6×6 matrix (b3 dropped; r(b2,b3) = 1) |
| `f_getMeanAndSD()` | `…:992` | sheet `mean_and_sd` → `mean_b1/sd_b1`, `mean_b2/sd_b2`, `mean_b3/sd_b3`, … |

Relevant values from `mean_and_sd`:

| variable | mean | sd |
|---|---|---|
| norms | 4.96 | 1.430 |
| b1_move_vehicle | 4.26 | 2.002 |
| b2_request_move | 0.069 | 0.255 |
| b3_notify_neighbor | 0.069 | 0.255 |

### 1.2 Population generation

`Main.f_generateSyntheticAgents()` — `_alp/Agents/Main/Code/Functions.java:558`

Gaussian copula: independent standard normals → Cholesky of the correlation matrix →
`u = Φ(z)` → `f_inverseECDF(sortedRealData.get(j), u)`. This reproduces the sample's marginal
distributions **and** its correlation structure. `agentAttributes` is therefore
`[0]=norms, [1]=rc, [2]=psi, [3]=pcp, [4]=b1, [5]=b2` (all as z-scores).

### 1.3 Agent initialisation

`Main.f_addEVOwner()` — `_alp/Agents/Main/Code/Functions.java:648`

```java
x.v_stand_prob_b1 = agentAttributes[4];
x.v_stand_prob_b2 = agentAttributes[5];
x.v_stand_prob_b3 = agentAttributes[5];   // prob_b3 = prob_b2
x.v_norm_b1       = agentAttributes[4];
x.v_norm_b2       = agentAttributes[5];
x.v_norm_b3       = agentAttributes[5];

x.v_prob_b1 = f_convertStandardizedToProb(x.v_stand_prob_b1, mean_b1, sd_b1, true);
x.v_prob_b2 = f_convertStandardizedToProb(x.v_stand_prob_b2, mean_b2, sd_b2, false);
x.v_prob_b3 = f_convertStandardizedToProb(x.v_stand_prob_b3, mean_b3, sd_b3, false);
```

Two points worth stating explicitly in the manuscript:

1. **The operational norm state is initialised at the agent's own behavioural propensity**
   (`v_norm_b1 = b1 draw`, `v_norm_b2 = v_norm_b3 = b2 draw`), not at the survey `norms`
   construct. `v_norms` is sampled and reported (`ar_avgNorms`) but never enters the
   behavioural update loop — the only norm that drives behaviour is `v_norm_b*`.
2. `f_convertStandardizedToProb` (`Main…:1566`) maps z → probability:
   `val = mean + z·sd`; if Likert, `p = (val − 1)/6`; else `p = val`; then clipped to [0, 1].

### 1.4 The resulting defaults

Computed directly from `frequency_list` + `mean_and_sd` (n = 288):

| Behaviour | Initial value at t = 0 |
|---|---|
| **B1** — move vehicle after charging | mean **p = 0.543** (Likert mean 4.26/7); full spread 0.00 → 1.00 across the 7 scale points |
| **B2** — request neighbour to move | binary: **6.94 %** of agents get p = 1.0, **93.06 %** get p = 0.0 |
| **B3** — notify neighbour | identical to B2 (same draw) |

These two numbers — mean p(B1) = 0.543 and 6.94 % prevalence for B2/B3 — **are** the
"potentially optimistic behavioural baseline" the reviewer is pointing at.

### 1.5 How the baseline diffuses

`EVOwner.f_updateNorms()` — `_alp/Agents/EVOwner/Code/Functions.java:893`

After every behavioural interaction the agent updates its norm by an EMA toward the
empirical max/min of the standardized scale, weighted by surprisal:

```
avgNorm = 0.5·personalNorm + 0.5·globalNorm          // sharePersonal hardcoded 0.5
EMA_t1  = EMA_t0 + smoothingFactor·(experienceValue − EMA_t0)·surprisalWeight
```

with `smoothingFactor = v_smoothingFactorNorms` (default **0.2**) and
`surprisalFunction` default `"log"`, `v_surprisalScale = 4.6`.
The updated norm then propagates: norm → `v_perc_social_interdep` (via `regCoef_norms_psi_*`)
→ `v_stand_prob_b*` (via `regCoef_psi_b*`) → `v_prob_b*`.

**This is the "behavioural uptake" channel**, and it is already covered by an existing
sensitivity run (`v_smoothingFactorNorms` ∈ {0.1, 0.2, 0.3}, button `b_startSimualtion9`).
What is *not* yet covered is the **initial level**, which is what this patch adds.

---

## 2. Design of the test

A single scalar, `v_prosocialityFactor` (λ), default **1.0**, applied at initialisation only.
λ = 1.0 must reproduce the published baseline bit-for-bit, so every change below is guarded
by `if (lambda != 1.0)`.

λ acts on the two constructs in the way that is native to each:

| Construct | Type | How λ applies |
|---|---|---|
| B1 | 7-point Likert → continuous p | `p'_b1 = clip01(λ · p_b1)`, then mapped back to the standardized scale |
| B2 / B3 | binary survey item (6.94 % yes) | prevalence' = `clip01(λ · 0.0694)`; the copula uniform `u` still decides *who* is prosocial, so the rank-correlation with norms/PSI/PCP is preserved |

Resulting arms:

| λ | mean p(B1) | B2/B3 prevalence | reading |
|---|---|---|---|
| 0.50 | 0.272 | 3.5 % | strongly less prosocial future adopters |
| 0.75 | 0.408 | 5.2 % | moderately less prosocial |
| **1.00** | **0.543** | **6.94 %** | **published baseline (current EV owners)** |
| 1.50 | 0.682 | 10.4 % | more prosocial |

Note the asymmetry: because p(B1) is clipped at 1, λ = 1.5 raises the mean by +0.14 while
λ = 0.5 lowers it by −0.27. The downward arm is therefore the stronger test — which is what
the reviewer is asking for. State this in the manuscript rather than hiding it.

One generator subtlety worth a sentence in the supplement: the baseline draws b2 through
`f_inverseECDF`, which **interpolates** between the two observed levels, so for
u ∈ (0.9303, 0.9338) it returns an intermediate value — roughly one agent in 288. The
threshold rule used for λ ≠ 1 emits only the two extremes. The λ arms are therefore a
marginally different generator rather than a pure rescaling of the baseline one. This is
why λ = 1.0 is guarded to run the original code path unchanged.

---

## 3. Code changes

Seven edits. Add the variables and the button through the AnyLogic GUI (it generates the
XML ids); paste the code bodies below.

### 3.1 `Startup_agent` — new variable

GUI: add a **Variable** on `Startup_agent`.

| property | value |
|---|---|
| Name | `v_prosocialityFactor` |
| Type | `double` |
| Initial value | `1.0` |

### 3.2 `Main` — new variable

Same, on `Main`.

| property | value |
|---|---|
| Name | `v_prosocialityFactor` |
| Type | `double` |
| Initial value | `1.0` |

### 3.3 `Startup_agent.f_initializeModel()` — pass it through

`_alp/Agents/Startup_agent/Code/Functions.java:1`

Add next to the other pass-through assignments (after `m.surprisalFunction = surprisalFunction;`, ~line 71):

```java
m.v_prosocialityFactor = v_prosocialityFactor;
```

### 3.4 `Main` — new function `f_convertProbToStandardized`

GUI: add a **Function** on `Main`.

| property | value |
|---|---|
| Name | `f_convertProbToStandardized` |
| Returns | `double` |
| Parameters | `double prob`, `double mean`, `double sd`, `boolean isLikert` |

Body:

```java
// Inverse of f_convertStandardizedToProb. Used only by the prosociality
// sensitivity test, to map a rescaled probability back onto the standardized
// scale on which all subsequent learning operates.
double val = isLikert ? (prob * 6.0 + 1.0) : prob;
return (val - mean) / sd;
```

Exact inverse wherever the [0, 1] clip in `f_convertStandardizedToProb` is inactive.
At the boundaries it is not: the 36 agents at z = −1.63056 map to p = 0 exactly, and the
round trip returns z = (1 − 4.26)/2.002 = −1.6284 rather than −1.63056. Immaterial (the
resulting probability is 0 either way), but it is why every change is guarded by
`if (lambda != 1.0)` — so the λ = 1.0 arm never round-trips at all.

### 3.5 `Main.f_generateSyntheticAgents()` — B2/B3 prevalence

`_alp/Agents/Main/Code/Functions.java:558`

Insert **before** the agent loop (after `NormalDistribution standardNormal = new NormalDistribution(0, 1);`):

```java
// --- Prosociality sensitivity: B2/B3 prevalence -------------------------
// b2 is a binary survey item, so its empirical inverse-CDF is a threshold on u.
// Generalising that threshold lets us vary the share of prosocial agents while
// keeping the copula's rank-correlation structure intact.
final int IDX_B2 = 5;
List<Double> b2Sorted = sortedRealData.get(IDX_B2);
double b2Min = b2Sorted.get(0);
double b2Max = b2Sorted.get(b2Sorted.size() - 1);
double b2BasePrevalence = 0.0;
for (double v : b2Sorted) { if (v > b2Min) b2BasePrevalence++; }
b2BasePrevalence /= b2Sorted.size();                       // 0.0694 in the current sample
double b2Prevalence = Math.max(0.0, Math.min(1.0, v_prosocialityFactor * b2BasePrevalence));
```

Then replace the inner transform loop:

```java
// BEFORE
for(int j= 0; j < numVars; j++){
    double u = standardNormal.cumulativeProbability(correlated.getEntry(j));
    agentAttributes[j] = f_inverseECDF(sortedRealData.get(j), u);
}
```

```java
// AFTER
for(int j= 0; j < numVars; j++){
    double u = standardNormal.cumulativeProbability(correlated.getEntry(j));
    if (j == IDX_B2 && v_prosocialityFactor != 1.0) {
        agentAttributes[j] = (u > 1.0 - b2Prevalence) ? b2Max : b2Min;
    } else {
        agentAttributes[j] = f_inverseECDF(sortedRealData.get(j), u);
    }
}
```

### 3.6 `Main.f_addEVOwner()` — B1 scaling

`_alp/Agents/Main/Code/Functions.java:648`

Replace the six assignments plus the three `f_convertStandardizedToProb` calls with:

```java
x.v_norms                  = agentAttributes[0];
x.v_reputational_concern   = agentAttributes[1];
x.v_perc_social_interdep   = agentAttributes[2];
x.v_perc_charging_pressure = agentAttributes[3];

// --- Prosociality sensitivity: B1 level --------------------------------
// lambda == 1.0 leaves the empirical baseline untouched (bit-identical).
// B2/B3 are handled at the prevalence level in f_generateSyntheticAgents.
double lambda = v_prosocialityFactor;

x.v_stand_prob_b1 = agentAttributes[4];
x.v_stand_prob_b2 = agentAttributes[5];
x.v_stand_prob_b3 = agentAttributes[5];   // prob_b3 = prob_b2

if (lambda != 1.0) {
    double p_b1 = f_convertStandardizedToProb(agentAttributes[4], mean_b1, sd_b1, true);
    p_b1 = Math.max(0.0, Math.min(1.0, lambda * p_b1));
    x.v_stand_prob_b1 = f_convertProbToStandardized(p_b1, mean_b1, sd_b1, true);
}

x.v_norm_b1 = x.v_stand_prob_b1;
x.v_norm_b2 = x.v_stand_prob_b2;
x.v_norm_b3 = x.v_stand_prob_b3;

x.v_prob_b1 = f_convertStandardizedToProb(x.v_stand_prob_b1, mean_b1, sd_b1, true);
x.v_prob_b2 = f_convertStandardizedToProb(x.v_stand_prob_b2, mean_b2, sd_b2, false);
x.v_prob_b3 = f_convertStandardizedToProb(x.v_stand_prob_b3, mean_b3, sd_b3, false);
```

Everything below (`x.v_type = EV;` onward) is unchanged.

### 3.7 Result plumbing

**`_alp/Classes/Class.J_MCResult.java`** — mirror the `negBiasFactor` pattern:

```java
// field, next to the other sensitivity fields (line 23, beside negBiasFactor)
private double prosocialityFactor;

// getter (line 140, beside getNegBiasFactor)
public double getProsocialityFactor() {
    return prosocialityFactor;
}

// setter (line 343, beside setNegBiasFactor)
public void setProsocialityFactor(double val) {
    this.prosocialityFactor = val;
}
```

**`Startup_agent.f_storeMCResults()`** (`…:681`) — after `results.setSocialChargingHours(...)`:

```java
results.setProsocialityFactor(v_prosocialityFactor);
```

**`Startup_agent.f_writeBehaviorScenariosToExcel()`** (`…:1291`) — after the last
`exportUncertaintyBoundsToExcel(r.getKMDMap(), …); col += 7;` and **before** `rowIndex++;`:

```java
excel_exportResultsBehaviours.setCellValue(r.getProsocialityFactor(), sheetIndexPerWeek, rowIndex, col++);
```

Appending at the end (column **131**) rather than inserting next to `socialChargingHours`
keeps all existing column positions — and therefore every existing plot script — valid.

**`SCM_export_results_behaviours.xlsx`** — put `prosocialityFactor` in cell **EA1**
(row 1, column 131), immediately after `kmd_50`.

### 3.8 New button

GUI: copy `b_startSimualtion9` (the EMA-smoothing sensitivity button), rename it
`b_sensProsociality`, label "Sensitivity: prosociality", and use this action code:

```java
traceln("Starting behavior selection simulation - sens prosociality");
v_rapidRun = true;
double time = System.currentTimeMillis();
double scenTime = time;
monteCarlo = true;
f_clearResultsCollections();

iterations = 50;

ShapeCheckBox[] checkBoxes = {
    cb_b1,
    cb_b2,
    cb_b3,
    cb_recheckCPAvailability
};

boolean[][] subselection = {
    {false, false, false, true},  // No behaviours
    {true,  false, false, true},  // Behaviour 1
    {false, true,  false, true},  // Behaviour 2
    {false, false, true,  true},  // Behaviour 3
    {true,  true,  true,  true}   // All behaviours
};

String[] labels = {
    "No behaviors",
    "Behavior 1",
    "Behavior 2",
    "Behavior 3",
    "All behaviors"
};

double[] prosocialityLevels = {0.5, 0.75, 1.0, 1.5};

for (int i = 0; i < prosocialityLevels.length; i++) {
    v_prosocialityFactor = prosocialityLevels[i];

    for (v_chargePoints = 6; v_chargePoints <= 20; v_chargePoints++) {
        if (!(v_chargePoints == 13 || v_chargePoints == 15 || v_chargePoints == 17 || v_chargePoints == 19)) {
            simulateBehaviorSelectionScenario(labels, subselection, checkBoxes);
            long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
            scenTime = (System.currentTimeMillis() - scenTime) / 1000.0;
            traceln("Finished scenarios for lambda = " + v_prosocialityFactor + ", " + v_EVsPerCP
                    + " EVs per CP in " + roundToDecimal(scenTime / 60, 2) + " minutes, memory: " + usedMemory + " MB");
            scenTime = System.currentTimeMillis();
        }
    }
    for (v_chargePoints = 100; v_chargePoints <= 100; v_chargePoints++) {
        simulateBehaviorSelectionScenario(labels, subselection, checkBoxes);
        scenTime = System.currentTimeMillis();
    }
}

v_prosocialityFactor = 1.0;   // restore the default

System.gc();
long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
double runTime = (System.currentTimeMillis() - time) / 1000.0;
traceln("Finished prosociality scenarios " + simulationCount + " in " + runTime + " seconds, memory: " + usedMemory + " MB");
```

That is 4 λ × 12 charge-point levels × 5 behaviour scenarios × 50 iterations = 12 000 runs.
The EMA sensitivity (3 × 12 × 5 × 50 = 9 000) is the closest reference point for runtime.
If that is too long, drop λ = 0.75 and run only the "No behaviours" / "All behaviours"
contrast — that is the comparison the reviewer's argument actually turns on.

---

## 4. Verification before the full run

1. Set `v_prosocialityFactor = 1.0`, run one Monte Carlo batch, and confirm the results
   match a stored baseline run. The `if (lambda != 1.0)` guards make this exact.
2. Set λ = 0.5 and check in the debugger / trace that
   `average(EVOwners, x -> x.v_prob_b1) ≈ 0.272` and
   `count(EVOwners, x -> x.v_prob_b2 > 0.5) / EVs ≈ 0.035` at t = 0.
3. Set λ = 1.5 and confirm mean p(B1) ≈ 0.682 and B2/B3 prevalence ≈ 0.104.

---

## 5. Output

Rename the export to `SCM_results_behaviours_sensProsociality.xlsx`, then run:

- `script_plot_charging_satisfaction_EVsPerCP_sens_prosociality.py`
  → `plot_charging_satisfaction_EVsPerCP_sens_prosociality.png`
  (charging fulfilment ratio vs EVs/CP, one linestyle per λ — same format as the other
  sensitivity figures)
- `script_plot_prosociality_diffusion.py`
  → `plot_prosociality_diffusion.png`
  (52-week trajectories, one line per λ, in three panels: mean p(B1), mean p(B2), and the
  charging fulfilment ratio — this is the figure that shows whether diffusion still happens
  from a lower starting point). B3 is omitted because `prob_b3 == prob_b2` by construction.
  The B1 panel carries a 90 % uncertainty band; the B2 panel deliberately does not, because
  the `u_probb2` column in the export is mis-wired — see §7.4.

Both scripts are in the project root.

---

## 6. What to say in the response to Reviewer 1

The result you are testing for is: **does the fulfilment gain from prosocial behaviour
survive a halved initial prosociality?** Three outcomes, three framings:

- **Gain shrinks roughly proportionally to λ.** Then the mechanism is real but its magnitude
  is sample-dependent. Report the λ = 0.5 arm as a conservative lower bound and rewrite the
  headline effect as a range rather than a point estimate.
- **Gain is preserved because norms converge upward regardless of the starting point.**
  Then the diffusion mechanism, not the initial level, carries the result — which is a
  stronger claim than the one currently made. Show the trajectory figure: the λ arms
  converge over the 52 weeks.
- **Gain collapses at λ = 0.5.** Then the paper's conclusion is genuinely conditional on
  current-adopter prosociality, and the honest move is to say so and frame the result as
  "conditional on prosociality levels comparable to those of current EV owners".

Points worth making regardless of outcome:

1. The *initial* level and the *uptake rate* are separate channels, and both are now tested:
   initial level via λ (this patch), uptake via `v_smoothingFactorNorms` ∈ {0.1, 0.2, 0.3}
   (already run). Say this explicitly — the reviewer asked for both and one already exists.
2. B2/B3 start at 6.94 % prevalence. That is not an optimistic baseline in absolute terms;
   almost the entire diffusion in those behaviours is generated endogenously by the norm
   dynamics, not inherited from the survey. This is a useful defensive point.
3. Direction of bias is arguable both ways. Current EV owners are early adopters and may be
   *more* prosocial than the future mass market; but the model is also run at 1–15 EVs per
   charge point, where charging pressure is far higher than these respondents experience,
   and pressure raises PSI and hence the behavioural probabilities. Present λ as bracketing
   the uncertainty rather than as a correction in one direction.

---

## 7. Other things noticed while reading (not part of this patch)

Small inconsistencies found in the code. None invalidate the results; flagging them so you
can decide whether they need a line in the manuscript or a fix.

1. **`v_sharePersonal = 0.7` is a fixed parameter that currently influences nothing.**
   It is declared on `Main` (`Variables.xml:2537`, initial value 0.7), never assigned
   anywhere, never passed through `f_initializeModel`, bound to no UI control, and not
   varied in any sensitivity run. It is read exactly once — `f_updatePCP`
   (`EVOwner…:815`) — into a local:

   ```java
   double avgPCP = EMA_t0 * sharePersonal + (1-sharePersonal) * global_avg;   // :819
   ```

   `avgPCP` is then **never used again** in that function; the EMA update runs off
   `EMA_t0` directly. The same dead-end applies to `main.v_avgPCP`, `mean_pcp` and
   `sd_pcp` read on the surrounding lines. So the perceived-charging-pressure update is
   purely individual — there is no global/social blending in that channel at all.

   The only live personal/global weight in the model is the **hardcoded `0.5`** in
   `f_updateNorms` (`EVOwner…:898`), which produces `avgNorm` and *is* used (`:905`).
   Two consequences: (a) if the manuscript describes a personal/global weighting, the
   number is 0.5 and it applies to norms only; (b) `v_sharePersonal` is not a lever —
   changing it changes nothing until `avgPCP` is actually wired into the EMA.
   `f_updateEMA` (`EVOwner…:872`) is likewise defined but never called.
2. **`avgProb_b*` is computed on two different scales.** `f_generateSyntehticPopulationEVs`
   (`Main…:620`) sets it from `v_stand_prob_b*` (z-scores); `f_countTotals` (`Main…:215`)
   sets it from `v_prob_b*` (probabilities). It is passed into `f_updateNorms` as
   `globalBehaviorRate` but never used in that function's body, so there is no behavioural
   consequence — the initial value is simply on the wrong scale for one timestep.
3. **`f_normalizeFromLikert()` is commented out** in `Main.f_initializeModel()` (line 53).
   Intentional given that `f_convertStandardizedToProb` now does the normalisation, but
   worth confirming the survey constructs are on the scale the regression coefficients assume.
4. **`meanavgProbB3` reads `getAvgProb_b2()`** in `f_writeBehaviorScenariosToExcel`, and
   `upperavgProbB2` reads `getAvgProb_b1().get(2)`. Since prob_b3 == prob_b2 by construction
   the first is harmless, but the `u_probb2` column in the exports is the B1 upper bound.
