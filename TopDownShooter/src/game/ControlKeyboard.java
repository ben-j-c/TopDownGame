package game;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ControlKeyboard implements KeyListener 
{
	Shoot inst;
	KeyList keys;
	
	ControlKeyboard(Shoot inst)
	{
		this.inst = inst;
		this.keys = inst.keys;
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		if(!inst.GAME_STARTED)
		{
			char keychar = e.getKeyChar();
			if(keychar == 'l')
			{
				
				inst.midGameReset();
			}
			else if(keychar == 'k')
			{
				inst.switchMappingMode();
			}
			else if(keychar == 'm')
			{
				inst.openMapDialog();
			}
			else if(Shoot.charIsOneOf(keychar, 'w','a','s','d'))
			{
				inst.changeOffset(e);
			}
		}
		
		char key = e.getKeyChar();
		
		switch (key)
		{
			case 'w':
			{
				keys.UP = true;
				keys.DOWN = false;
				break;
			}
			case 's':
			{
				keys.DOWN = true;
				keys.UP = false;
				break;
			}
			case 'a':
			{
				keys.LEFT = true;
				keys.RIGHT = false;
				break;
			}
			case 'd':
			{
				keys.RIGHT = true;
				keys.LEFT = false;
				break;
			}
			case 'q':
			{
				inst.weapon = !inst.weapon;
				break;
			}
			default:
			{
				break;
			}
		}
		
	}
	@Override
	public void keyReleased(KeyEvent e)
	{
		
		char key = e.getKeyChar();
		
		switch (key)
		{
			case 'w':
			{
				keys.UP = false;
				break;
			}
			case 's':
			{
				keys.DOWN = false;
				break;
			}
			case 'a':
			{
				keys.LEFT = false;
				break;
			}
			case 'd':
			{
				keys.RIGHT = false;
				break;
			}
			default:
			{
				break;
			}
		}
		
		
	}
	@Override
	public void keyTyped(KeyEvent e)
	{
		
	}
	
}
