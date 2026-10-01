package com.lladlam.melox.ui.glass

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import com.kyant.backdrop.Backdrop
import com.lladlam.melox.core.diagnostics.MeloXCrashStore

/*
 * 液态玻璃的**能力门控**（2026-10-01 新增）。
 *
 * 这里只回答一个问题：**这一帧到底能不能画玻璃**。
 *   · 窗口没有硬件加速 ⇒ RenderNode / RenderEffect / RuntimeShader 全部无从谈起
 *     ⇒ 回落到各材质已经写好的 `background(...)` 实心分支。
 *     这类设备本来就没有玻璃条件，属于「不支持」，**不是降级**。
 *   · 其余情况一律放行，不改动任何数值、不改变观感。
 *
 * ⚠ 另一条必须守住的约束：**所有会真正画玻璃的 composable 都必须经
 *   `meloXGlassBackdrop()` 取背板**，不要直接读 `LocalMeloXBackdrop.current`。
 *   前者带着硬件加速门控，后者绕不过去。
 *   （`meloXGlassAvailable()` 给「只想知道一个布尔」的地方用，不触发渲染打点。）
 */

/**
 * 玻璃此刻是否可用：既有背板，窗口又确实挂了硬件加速。
 *
 * ⚠ 不要用 `LocalMeloXBackdrop.current != null` 代替它：在无硬件加速时那个判断仍为 true，
 *   会让上层按「有玻璃」去排版（例如选半透明 tint），而玻璃其实一次都不会画
 *   ⇒ 出现「半透明 tint 但没有采样背板」的不可读 UI。
 */
@Composable
internal fun meloXGlassAvailable(): Boolean {
    if (!LocalView.current.isHardwareAccelerated) return false
    return LocalMeloXBackdrop.current != null
}

/**
 * 取当前可用的玻璃背板；不可用时返回 null。
 *
 * ⚠ **所有会真正画玻璃的 composable 都必须经这里取 backdrop。**
 *   返回 null 时每个材质函数都已写好 `background(...)` 实心回落分支，
 *   于是 `drawBackdrop` 一次都不跑，RenderEffect / AGSL / GraphicsLayer 全绕开。
 */
@Composable
internal fun meloXGlassBackdrop(): Backdrop? {
    if (!meloXGlassAvailable()) return null
    // 只做记录：给崩溃现场标注「当时玻璃在渲染」。不参与任何门控/降级。
    MeloXCrashStore.markGlassRendering()
    return LocalMeloXBackdrop.current
}

/**
 * 把按压进度洗回 [0, 1]。
 *
 * 必要性（实测代码路径）：
 * - `PublicInteractiveHighlight` 用 `spring(0.5f, 300f)` —— **欠阻尼 ζ=0.5**，
 *   过冲约 +16.3%、回弹约 −16.3% ⇒ `pressProgress` 真实区间 ≈ [−0.16, 1.16]。
 *   它经 `meloXLiquidButton` 喂给 `meloXGlassSurface`（登录页 / 云盘 / 资料库 / 收藏页的按钮）。
 *
 * ⚠ **`lens()` 的 AGSL 在 `refractionHeight == 0` 时产生 NaN**（逐句核对 shader 原文后的准确机理）：
 *   ```
 *   float sd = sdRoundedRect(...);          // 负=形状内，正=形状外
 *   if (-sd >= refractionHeight) return content.eval(coord);   // 深处提前返回
 *   sd = min(sd, 0.0);                      // ← 形状外的像素被夹到 0
 *   float d = circleMap(1.0 - -sd / refractionHeight) * refractionAmount;
 *   ```
 *   - 形状内那一圈（`-sd ∈ [0, refractionHeight)`）⇒ `x = 1 - (-sd)/H ∈ (0, 1]`，
 *     `circleMap(x) = 1 - sqrt(1 - x²)` 恒在定义域内 ⇒ **H > 0 时无论多小都安全**。
 *   - 形状**外**的像素（圆角矩形的角、抗锯齿边缘，`sd > 0`）被夹成 `sd = 0`
 *     ⇒ `x = 1 - 0/H`：**H > 0 ⇒ x = 1，安全；H = 0 ⇒ 0/0 = NaN**
 *     ⇒ `d = NaN` ⇒ `refractedCoord = NaN` ⇒ `content.eval(NaN)`。
 *   - 而 `H = 0` 恰好是**静息态**（`lens(H·p, A·p)`，`p = 0`）——底栏面板/捕获层/水珠
 *     每一帧都在画，所以这是每帧命中的路径。修法只能是「整段跳过 lens」，不能是「把 H 调小」。
 *   ⚠ 反过来说：**`refractionHeight` 是常量、不乘 p 的调用点不需要这道门**
 *     （例如 `meloXGlassSurface` 的 `spec.refractionHeight`）。
 *
 * ⚠ 另有一个**与 p 无关、属上游库自身**的一像素级 NaN：
 *   `normalize(gradSdRoundedRect(...) + depthEffect * normalize(centeredCoord))` 里
 *   `centeredCoord == (0,0)`（图层正中心像素）会让 `normalize()` 拿到零向量 ⇒ NaN；
 *   IEEE `0 × NaN = NaN`，`depthEffect = 0` 也挡不住。影响单像素、不可见，不改（改了即偏离上游）。
 *
 * ⚠ 底栏水珠用的是 `PublicDampedDragAnimation` / `LiquidDragAnimation`，其 press spec 是
 *   `spring(1f, 1000f)`（临界阻尼、不过冲、静息恰好落在 0），所以本函数对它们恒等 ——
 *   它们真正需要的是 `lens` 的零值门，而不是区间清洗。
 */
internal fun safeProgress(value: Float): Float =
    if (value.isNaN() || value.isInfinite()) 0f else value.coerceIn(0f, 1f)

/**
 * 把任意 Float 洗成有限值（NaN / ±Inf → 0）。
 *
 * 用途：任何要写进 `GraphicsLayerScope`（= HWUI `RenderNode` 变换矩阵）的量。
 * 一旦 NaN 进到矩阵里，拍平后的矩阵不可逆，绘制结果未定义 —— 这是「能跑玻璃的机型
 * 上因为自身数值问题崩」的典型入口，所以所有 `layerBlock` 出口都过一遍。
 *
 * ⚠ **`coerceIn` / `fastCoerceIn` 都不过滤 NaN**（`a < min` 与 `a > max` 同时为 false
 *   ⇒ 原样返回 NaN），所以「先 coerceIn 再乘除」这种形状必须**源头洗 + 出口兜**，
 *   不能只靠夹取。
 */
internal fun Float.finiteOrZero(): Float = if (isNaN() || isInfinite()) 0f else this
