/* eslint-disable quote-props */
module.exports = {
  env: {
    browser: true,
    es2021: true,
  },
  extends: ['google'],
  parser: '@typescript-eslint/parser',
  parserOptions: {
    ecmaVersion: 'latest',
    sourceType: 'module',
  },
  plugins: ['@typescript-eslint', 'prettier'],
  ignorePattern: ['./dist'],
  rules: {
    'no-console': 0,
    'require-jsdoc': 0,
    'prettier/prettier': 'error',
    'max-len': [
      2,
      {
        code: 140,
        ignorePattern: `^import .*`,
      },
    ],
    'comma-dangle': [
      'error',
      {
        arrays: 'only-multiline',
        objects: 'only-multiline',
        imports: 'only-multiline',
        exports: 'only-multiline',
        functions: 'only-multiline',
      },
    ],
    semi: [2, 'never'],
    indent: ['error', 2],
    'operator-linebreak': ['error', 'before'],
  },
}
