import React from 'react';
import { useTranslation } from 'react-i18next';
import { Globe } from 'lucide-react';

const LanguageSwitcher = () => {
  const { i18n, t } = useTranslation();

  const changeLanguage = (lng) => {
    i18n.changeLanguage(lng);
  };

  return (
    <div className="relative group">
      <button
        className="flex items-center space-x-2 px-3 py-2 rounded-md text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
        title={t('ui.language')}
      >
        <Globe className="w-5 h-5" />
        <span className="text-sm font-medium">{i18n.language.toUpperCase()}</span>
      </button>
      
      <div className="absolute right-0 mt-2 w-32 bg-white dark:bg-gray-800 rounded-md shadow-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-50">
        <div className="py-1">
          <button
            onClick={() => changeLanguage('tr')}
            className={`block w-full text-left px-4 py-2 text-sm hover:bg-gray-100 dark:hover:bg-gray-700 ${
              i18n.language === 'tr' ? 'bg-gray-100 dark:bg-gray-700' : ''
            }`}
          >
            🇹🇷 Türkçe
          </button>
          <button
            onClick={() => changeLanguage('en')}
            className={`block w-full text-left px-4 py-2 text-sm hover:bg-gray-100 dark:hover:bg-gray-700 ${
              i18n.language === 'en' ? 'bg-gray-100 dark:bg-gray-700' : ''
            }`}
          >
            🇺🇸 English
          </button>
        </div>
      </div>
    </div>
  );
};

export default LanguageSwitcher; 