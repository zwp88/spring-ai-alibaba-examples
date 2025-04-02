import { useAtom } from 'jotai';
import { useEffect } from 'react';
import { actualThemeAtom, setupThemeListener, themeModeAtom, ThemeMode } from '../stores/theme.store';

export const useTheme = () => {
  const [themeMode, setThemeMode] = useAtom(themeModeAtom);
  const [actualTheme] = useAtom(actualThemeAtom);

  // 在组件挂载时设置监听器
  useEffect(() => {
    // 只有当使用系统主题时才需要监听系统主题变化
    if (themeMode === 'system') {
      return setupThemeListener(() => {
        // 强制更新组件以应用新主题
        setThemeMode('system');
      });
    }
  }, [themeMode, setThemeMode]);

  // 切换主题的函数
  const toggleTheme = () => {
    setThemeMode(prev => prev === 'dark' ? 'light' : 'dark');
  };

  // 设置特定主题的函数
  const setTheme = (mode: ThemeMode) => {
    setThemeMode(mode);
  };

  return {
    themeMode,     // 当前主题模式（light/dark/system）
    actualTheme,   // 实际应用的主题（light/dark）
    toggleTheme,   // 切换主题函数
    setTheme,      // 设置特定主题函数
  };
}; 