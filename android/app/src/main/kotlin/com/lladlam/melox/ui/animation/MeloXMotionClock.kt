package com.lladlam.melox.ui.animation

import androidx.compose.ui.MotionDurationScale
import kotlin.coroutines.coroutineContext

/**
 * 把「标称动画时长」换算成**真实的等待毫秒**。
 *
 * 覆盖层过渡（列表卡片 ←→ 详情 hero 的一镜到底）里有一批手写的
 * `delay(PageXxxMillis + settle)`：等这段过渡跑完再释放详情内容 / 解锁排队。
 * 但 `delay` 走的是**墙上时钟**，`tween(...)` 走的是 **Compose 动画时钟** ——
 * 后者会被系统「动画时长缩放」整体乘一个系数。两者一旦脱钩，跨机型就会各自跑偏：
 *
 * · 调到 **2×**（测评机 / 游戏机型上很常见）：过渡实际 2 倍长，delay 却按 1× 到时
 *   ⇒ 详情内容被提前释放，一镜到底 **morph 被从中间掐断**；同时
 *   `overlayTransitionBusy` 提前解锁，允许在动画没跑完时开下一张卡 ⇒ **并发过渡**
 *   （正是「占用 key」当年要解决的那个问题）。
 * · 调到 **0.5×**：delay 反而过长 ⇒ 返回后卡片迟迟不重新出现，点击也延迟响应。
 * · **关掉动画 / 无障碍「移除动画」**（`scaleFactor = 0`）：动画瞬时完成，delay
 *   变成纯粹的迟滞 —— 明明已经画完了，状态还锁着。
 *
 * ⚠ 这里**不读系统设置、也不写死系数**，直接取 Compose 自己用的那一个
 *   （[MotionDurationScale] 由 `WindowRecomposer` 注入重组器的 CoroutineContext，
 *   与 `tween` 实际乘的是同一个值）。于是 scale = 1 的设备上行为**逐毫秒不变**，
 *   非 1 的设备上与动画严格同拍 —— 跨机型一致性的唯一真相源。
 *
 * ⚠ 必须在**组合提供的作用域**里调用（`LaunchedEffect` / `rememberCoroutineScope`）。
 *   取不到时退回 1×（即旧行为），不会更坏。
 *
 * @param nominalMillis 过渡的标称时长（[MeloXMotion] 里的 `PageEnterMillis` 这类常量）。
 * @param settleMillis  动画之外的固定余量（composition / 布局 / 内容加载），**不参与缩放**。
 */
internal suspend fun meloXSettledMillis(nominalMillis: Int, settleMillis: Long): Long {
    val scale = coroutineContext[MotionDurationScale]?.scaleFactor ?: 1f
    // scale = 0：过渡是瞬时的，只保留最小余量，不要按标称时长干等。
    if (scale <= 0f) return settleMillis
    return (nominalMillis * scale).toLong() + settleMillis
}
