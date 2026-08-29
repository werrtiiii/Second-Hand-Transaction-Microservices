"""导入工具的SQL字面量、转义及恶意表达式边界测试。"""
import sys,unittest
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
from import_legacy_data import literal,split_values,sql_value,insert

class ImportSafetyTests(unittest.TestCase):
    def test_strings_preserve_escaped_content(self):
        # 中文、引号和反斜杠不丢失。
        self.assertEqual(literal("'中文, O\\'Reilly\\n下一行\\\\path'"),"中文, O'Reilly\n下一行\\path")
    def test_mysql_binary_values(self):
        self.assertEqual(literal("_binary '\\0'"),b'\0')
        self.assertEqual(literal("_binary '\x01'"),b'\x01')
    def test_tuple_split_ignores_quoted_commas(self):
        self.assertEqual(split_values("(1,'a,b'),(2,'c)')"),["(1,'a,b')","(2,'c)')"])
    def test_executable_expression_is_rejected(self):
        with self.assertRaises(ValueError):literal("LOAD_FILE('/etc/passwd')")
        with self.assertRaises(ValueError):literal("'x'); DROP TABLE users; --'")
    def test_insert_uses_hex_and_fixed_identifiers(self):
        result=insert('secondhand_user','users',{'nickname':"'; DROP TABLE users;--"})
        self.assertNotIn('DROP TABLE',result)
        self.assertIn("CONVERT(X'",result)
        with self.assertRaises(ValueError):insert('secondhand','users',{'id':1})

if __name__=='__main__':unittest.main()
