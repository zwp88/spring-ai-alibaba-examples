import { atom } from 'jotai';

// 定义主题模式类型
export type ThemeMode = 'light' | 'dark' | 'system';

// 创建主题模式状态
export const themeModeAtom = atom<ThemeMode>('system');

// 创建实际主题状态（考虑系统主题）
export const actualThemeAtom = atom<'light' | 'dark'>(get => {
  const themeMode = get(themeModeAtom);
  
  if (themeMode === 'system') {
    // 检查系统偏好
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }
  
  return themeMode;
});

// 监听系统主题变化的函数
export const setupThemeListener = (callback: () => void) => {
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
  
  // 添加变化监听器
  const listener = () => callback();
  mediaQuery.addEventListener('change', listener);
  
  // 返回清理函数
  return () => mediaQuery.removeEventListener('change', listener);
}; 